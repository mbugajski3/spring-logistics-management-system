package com.mbugajski.logistics.assignment;

import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.address.repository.AddressRepository;
import com.mbugajski.logistics.assignment.entity.ShipmentAssignment;
import com.mbugajski.logistics.assignment.repository.ShipmentAssignmentRepository;
import com.mbugajski.logistics.assignment.service.ShipmentAssignmentService;
import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.courier.repository.CourierRepository;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.customer.repository.CustomerRepository;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.entity.ShipmentStatus;
import com.mbugajski.logistics.shipment.repository.ShipmentRepository;
import com.mbugajski.logistics.shipment.service.ShipmentService;
import com.mbugajski.logistics.vehicle.entity.Vehicle;
import com.mbugajski.logistics.vehicle.entity.VehicleType;
import com.mbugajski.logistics.vehicle.exception.VehicleInvalidStateException;
import com.mbugajski.logistics.vehicle.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({ShipmentAssignmentService.class, ShipmentService.class})
public class ShipmentAssignmentServiceIntegrationTest {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ShipmentAssignmentRepository shipmentAssignmentRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ShipmentAssignmentService shipmentAssignmentService;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldRollbackWhenVehicleCannotBeMarkedAsBusy() {
        Address customerAddress = createCustomerAddress();
        addressRepository.saveAndFlush(customerAddress);

        Customer customer = createCustomer(customerAddress);
        customerRepository.saveAndFlush(customer);

        Address deliveryAddress = createDeliveryAddress();
        addressRepository.saveAndFlush(deliveryAddress);

        Shipment shipment = createShipment(customerAddress, customer, deliveryAddress);
        shipmentRepository.saveAndFlush(shipment);

        Courier courier = createCourier();
        courierRepository.saveAndFlush(courier);

        Vehicle vehicle = createVehicle();
        vehicle.deactivate();
        vehicleRepository.saveAndFlush(vehicle);

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class,() -> shipmentAssignmentService.assign(shipment.getId(), courier.getId(), vehicle.getId()));

        Courier courierFound = courierRepository.findById(courier.getId()).orElseThrow();
        Shipment shipmentFound = shipmentRepository.findById(shipment.getId()).orElseThrow();

        assertEquals("Vehicle must be active and available to mark as busy.", exception.getMessage());
        assertTrue(courierFound.isAvailable());
        assertEquals(ShipmentStatus.CREATED, shipmentFound.getStatus());
        assertEquals(0L, shipmentAssignmentRepository.count());
    }

    @Test
    @Transactional
    void shouldAssignShipment() {
        Address customerAddress = createCustomerAddress();
        addressRepository.saveAndFlush(customerAddress);

        Customer customer = createCustomer(customerAddress);
        customerRepository.saveAndFlush(customer);

        Address deliveryAddress = createDeliveryAddress();
        addressRepository.saveAndFlush(deliveryAddress);

        Shipment shipment = createShipment(customerAddress, customer, deliveryAddress);
        shipmentRepository.saveAndFlush(shipment);

        Courier courier = createCourier();
        courierRepository.saveAndFlush(courier);

        Vehicle vehicle = createVehicle();
        vehicleRepository.saveAndFlush(vehicle);

        assertEquals(0L, shipmentAssignmentRepository.count());
        assertTrue(courier.isAvailable());
        assertTrue(vehicle.isAvailable());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());

        ShipmentAssignment shipmentAssignment = shipmentAssignmentService.assign(shipment.getId(), courier.getId(), vehicle.getId());

        Shipment shipmentFound = shipmentRepository.findById(shipment.getId()).orElseThrow();
        Courier courierFound = courierRepository.findById(courier.getId()).orElseThrow();
        Vehicle vehicleFound = vehicleRepository.findById(vehicle.getId()).orElseThrow();

        assertEquals(ShipmentStatus.READY_FOR_PICKUP, shipmentFound.getStatus());
        assertFalse(courierFound.isAvailable());
        assertFalse(vehicleFound.isAvailable());
        assertEquals(1L, shipmentAssignmentRepository.count());
        assertTrue(shipmentAssignmentRepository.existsById(shipmentAssignment.getId()));
    }

    @Test
    void shouldReleaseResourcesAfterShipmentDelivery() {
        Address customerAddress = createCustomerAddress();
        addressRepository.saveAndFlush(customerAddress);

        Customer customer = createCustomer(customerAddress);
        customerRepository.saveAndFlush(customer);

        Address deliveryAddress = createDeliveryAddress();
        addressRepository.saveAndFlush(deliveryAddress);

        Shipment shipment = createShipment(customerAddress, customer, deliveryAddress);
        shipmentRepository.saveAndFlush(shipment);

        Courier courier = createCourier();
        courierRepository.saveAndFlush(courier);

        Vehicle vehicle = createVehicle();
        vehicleRepository.saveAndFlush(vehicle);

        assertEquals(0L, shipmentAssignmentRepository.count());
        assertTrue(courier.isAvailable());
        assertTrue(vehicle.isAvailable());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());

        ShipmentAssignment shipmentAssignment = shipmentAssignmentService.assign(shipment.getId(), courier.getId(), vehicle.getId());

        Shipment shipmentFound = shipmentRepository.findById(shipment.getId()).orElseThrow();
        Courier courierFound = courierRepository.findById(courier.getId()).orElseThrow();
        Vehicle vehicleFound = vehicleRepository.findById(vehicle.getId()).orElseThrow();

        assertEquals(ShipmentStatus.READY_FOR_PICKUP, shipmentFound.getStatus());
        assertFalse(courierFound.isAvailable());
        assertFalse(vehicleFound.isAvailable());
        assertEquals(1L, shipmentAssignmentRepository.count());
        assertTrue(shipmentAssignmentRepository.existsById(shipmentAssignment.getId()));

        shipmentService.markAsInTransit(shipmentFound.getId());
        shipmentService.markAsDelivered(shipmentFound.getId());

        shipmentAssignmentRepository.flush();
        entityManager.clear();

        Shipment deliveredShipment = shipmentRepository.findById(shipment.getId()).orElseThrow();
        Courier deliveredCourier = courierRepository.findById(courier.getId()).orElseThrow();
        Vehicle deliveredVehicle = vehicleRepository.findById(vehicle.getId()).orElseThrow();


        assertEquals(ShipmentStatus.DELIVERED, deliveredShipment.getStatus());
        assertTrue(deliveredCourier.isAvailable());
        assertTrue(deliveredVehicle.isAvailable());

        assertEquals(1L, shipmentAssignmentRepository.count());
    }

    @Test
    void shouldReleaseResourcesAfterShipmentCancelled() {
        Address customerAddress = createCustomerAddress();
        addressRepository.saveAndFlush(customerAddress);

        Customer customer = createCustomer(customerAddress);
        customerRepository.saveAndFlush(customer);

        Address deliveryAddress = createDeliveryAddress();
        addressRepository.saveAndFlush(deliveryAddress);

        Shipment shipment = createShipment(customerAddress, customer, deliveryAddress);
        shipmentRepository.saveAndFlush(shipment);

        Courier courier = createCourier();
        courierRepository.saveAndFlush(courier);

        Vehicle vehicle = createVehicle();
        vehicleRepository.saveAndFlush(vehicle);

        assertEquals(0L, shipmentAssignmentRepository.count());
        assertTrue(courier.isAvailable());
        assertTrue(vehicle.isAvailable());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());

        ShipmentAssignment shipmentAssignment = shipmentAssignmentService.assign(shipment.getId(), courier.getId(), vehicle.getId());

        Shipment shipmentFound = shipmentRepository.findById(shipment.getId()).orElseThrow();
        Courier courierFound = courierRepository.findById(courier.getId()).orElseThrow();
        Vehicle vehicleFound = vehicleRepository.findById(vehicle.getId()).orElseThrow();

        assertEquals(ShipmentStatus.READY_FOR_PICKUP, shipmentFound.getStatus());
        assertFalse(courierFound.isAvailable());
        assertFalse(vehicleFound.isAvailable());
        assertEquals(1L, shipmentAssignmentRepository.count());
        assertTrue(shipmentAssignmentRepository.existsById(shipmentAssignment.getId()));

        shipmentService.markAsCancelled(shipmentFound.getId());

        shipmentAssignmentRepository.flush();
        entityManager.clear();

        Shipment cancelledShipment = shipmentRepository.findById(shipment.getId()).orElseThrow();
        Courier cancelledCourier = courierRepository.findById(courier.getId()).orElseThrow();
        Vehicle cancelledVehicle = vehicleRepository.findById(vehicle.getId()).orElseThrow();

        assertEquals(ShipmentStatus.CANCELLED, cancelledShipment.getStatus());
        assertTrue(cancelledCourier.isAvailable());
        assertTrue(cancelledVehicle.isAvailable());

        assertEquals(1L, shipmentAssignmentRepository.count());
    }

    private Shipment createShipment(Address customerAddress, Customer customer, Address deliveryAddress) {
        return new Shipment(customer, customerAddress, deliveryAddress, new BigDecimal("5.00"));
    }

    private Customer createCustomer(Address customerAddress) {
        return new Customer("Adam", "Kowalski", "adam@kowalski.com", "+48 532 234 124", customerAddress);
    }

    private Address createCustomerAddress() {
        return new Address("Klientowa", "20", "6", "Warszawa", "33-421", "Poland");
    }

    private Address createDeliveryAddress() {
        return new Address("Wysyłkowa", "13", "3", "Krakow", "60-123", "Poland");
    }

    private Shipment createOverweightShipment() {
        Address customerAddress = new Address("Adrianowa", "20", "14", "Kielce", "50-231", "Poland");
        Address deliveryAddress = new Address("Wysyłkowa", "13", "3", "Krakow", "60-123", "Poland");

        Customer customer = new Customer("Adrian", "Nowak", "adrian@nowak.com", "+48 553 214 532", customerAddress);

        return new Shipment(customer, customerAddress, deliveryAddress, new BigDecimal("800.00"));
    }

    private Courier createCourier() {
        return new Courier("Postman", "Pat", "+48 999 234 523");
    }

    private Vehicle createVehicle() {
        return new Vehicle("Ford", "Transport", "GD 9231L", VehicleType.VAN, new BigDecimal("700.00"));
    }

    private Vehicle createVehicleWithLowMaximumLoad() {
        return new Vehicle("Ford", "Transport", "GD 9231L", VehicleType.VAN, new BigDecimal("2.00"));
    }

}
