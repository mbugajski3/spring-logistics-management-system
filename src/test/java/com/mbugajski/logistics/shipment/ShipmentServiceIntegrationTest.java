package com.mbugajski.logistics.shipment;

import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.address.repository.AddressRepository;


import com.mbugajski.logistics.assignment.entity.AssignmentStatus;
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
import com.mbugajski.logistics.shipment.repository.ShipmentSortBy;
import com.mbugajski.logistics.shipment.service.ShipmentService;
import com.mbugajski.logistics.vehicle.entity.Vehicle;
import com.mbugajski.logistics.vehicle.entity.VehicleType;


import com.mbugajski.logistics.vehicle.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.hibernate.query.SortDirection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@Import({ShipmentService.class, ShipmentAssignmentService.class})
public class ShipmentServiceIntegrationTest {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ShipmentAssignmentRepository shipmentAssignmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ShipmentAssignmentService shipmentAssignmentService;

    @Test
    void shouldReturnShipmentWithStatusCreated() {
        Address customerAddress = createCustomerAddress();
        addressRepository.saveAndFlush(customerAddress);

        Address deliveryAddress = createDeliveryAddress();
        addressRepository.saveAndFlush(deliveryAddress);

        Customer customer1 = createCustomer(customerAddress);
        customerRepository.saveAndFlush(customer1);

        Shipment shipment1 = createShipment(customerAddress, customer1, deliveryAddress);

        Shipment shipment2 = createShipment(customerAddress, customer1, deliveryAddress);
        shipment2.markAsReadyForPickup();
        shipment2.markAsInTransit();

        Shipment shipment3 = createShipment(customerAddress, customer1, deliveryAddress);

        shipmentRepository.saveAndFlush(shipment1);
        shipmentRepository.saveAndFlush(shipment2);
        shipmentRepository.saveAndFlush(shipment3);

        Page<Shipment> shipmentPage = shipmentService.findAllByPageNumber(0, 5, ShipmentStatus.CREATED, null);

        assertEquals(2, shipmentPage.getTotalElements());
        assertEquals(2, shipmentPage.getContent().size());
        assertFalse(shipmentPage.getContent().contains(shipment2));
        assertTrue(shipmentPage.getContent().stream().allMatch(shipment -> shipment.getStatus() == ShipmentStatus.CREATED));
    }

    @Test
    void shouldReturnShipmentWithCustomerId() {
        Address customerAddress = createCustomerAddress();
        addressRepository.saveAndFlush(customerAddress);

        Address deliveryAddress = createDeliveryAddress();
        addressRepository.saveAndFlush(deliveryAddress);

        Customer customer1 = createCustomer(customerAddress);
        customerRepository.saveAndFlush(customer1);

        Customer customer2 = new Customer("Michał", "Kot", "michal@kot.com", "+48 532 234 253", customerAddress);
        customerRepository.saveAndFlush(customer2);

        Shipment shipment1 = createShipment(customerAddress, customer1, deliveryAddress);
        Shipment shipment2 = createShipment(customerAddress, customer1, deliveryAddress);
        Shipment shipment3 = createShipment(customerAddress, customer2, deliveryAddress);

        shipmentRepository.saveAndFlush(shipment1);
        shipmentRepository.saveAndFlush(shipment2);
        shipmentRepository.saveAndFlush(shipment3);

        Page<Shipment> shipmentPage = shipmentService.findAllByPageNumber(0, 5, null, customer1.getId());

        assertEquals(2, shipmentPage.getTotalElements());
        assertEquals(2, shipmentPage.getContent().size());
        assertTrue(shipmentPage.getContent().stream().allMatch(shipment -> shipment.getCustomer().getId().equals(customer1.getId())));
        assertFalse(shipmentPage.getContent().stream().anyMatch(shipment -> shipment.getCustomer().getId().equals(customer2.getId())));
    }

    @Test
    void shouldReturnShipmentUsingBothFilters() {
        Address customerAddress = createCustomerAddress();
        addressRepository.saveAndFlush(customerAddress);

        Address deliveryAddress = createDeliveryAddress();
        addressRepository.saveAndFlush(deliveryAddress);

        Customer customer1 = createCustomer(customerAddress);
        customerRepository.saveAndFlush(customer1);

        Customer customer2 = new Customer("Michał", "Kot", "michal@kot.com", "+48 532 234 253", customerAddress);
        customerRepository.saveAndFlush(customer2);

        Shipment shipment1 = createShipment(customerAddress, customer1, deliveryAddress);

        Shipment shipment2 = createShipment(customerAddress, customer1, deliveryAddress);
        shipment2.markAsReadyForPickup();
        shipment2.markAsInTransit();

        Shipment shipment3 = createShipment(customerAddress, customer2, deliveryAddress);

        shipmentRepository.saveAndFlush(shipment1);
        shipmentRepository.saveAndFlush(shipment2);
        shipmentRepository.saveAndFlush(shipment3);

        Page<Shipment> shipmentPage = shipmentService.findAllByPageNumber(0, 5, ShipmentStatus.CREATED, customer1.getId());

        assertEquals(1, shipmentPage.getTotalElements());
        assertEquals(1, shipmentPage.getContent().size());
        assertTrue(shipmentPage.getContent().stream().anyMatch(shipment -> shipment.getCustomer().getId().equals(customer1.getId())));
        assertTrue(shipmentPage.getContent().stream().anyMatch(shipment -> shipment.getStatus() == ShipmentStatus.CREATED));
        assertFalse(shipmentPage.getContent().contains(shipment2));
        assertFalse(shipmentPage.getContent().contains(shipment3));
    }

    @Test
    void shouldReturnShipmentsDescending() {
        Address customerAddress = createCustomerAddress();
        addressRepository.saveAndFlush(customerAddress);

        Address deliveryAddress = createDeliveryAddress();
        addressRepository.saveAndFlush(deliveryAddress);

        Customer customer1 = createCustomer(customerAddress);
        customerRepository.saveAndFlush(customer1);

        Shipment shipment1 = new Shipment(customer1, customerAddress, deliveryAddress, new BigDecimal("2"));

        Shipment shipment2 = new Shipment(customer1, customerAddress, deliveryAddress, new BigDecimal("7"));

        Shipment shipment3 = new Shipment(customer1, customerAddress, deliveryAddress, new BigDecimal("15"));

        shipmentRepository.saveAndFlush(shipment1);
        shipmentRepository.saveAndFlush(shipment2);
        shipmentRepository.saveAndFlush(shipment3);

        Page<Shipment> shipmentPage = shipmentService.findAllByPageNumber(0, 5, null, null, ShipmentSortBy.weight, "desc");

        assertEquals(shipment3, shipmentPage.getContent().get(0));
        assertEquals(shipment2, shipmentPage.getContent().get(1));
        assertEquals(shipment1, shipmentPage.getContent().get(2));
    }

    @Transactional
    @Test
    void shouldDeliverShipment() {
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

        Shipment shipmentFound = shipmentRepository.findById(shipment.getId()).orElseThrow();
        Courier courierFound = courierRepository.findById(courier.getId()).orElseThrow();
        Vehicle vehicleFound = vehicleRepository.findById(vehicle.getId()).orElseThrow();

        ShipmentAssignment shipmentAssignment = shipmentAssignmentService.assign(shipmentFound.getId(), courierFound.getId(), vehicleFound.getId());

        Shipment assignedShipment = shipmentRepository.findById(shipmentFound.getId()).orElseThrow();
        Courier assignedCourier = courierRepository.findById(courierFound.getId()).orElseThrow();
        Vehicle assignedVehicle = vehicleRepository.findById(vehicleFound.getId()).orElseThrow();

        assertEquals(ShipmentStatus.READY_FOR_PICKUP, assignedShipment.getStatus());
        assertFalse(assignedCourier.isAvailable());
        assertFalse(assignedVehicle.isAvailable());
        assertEquals(1L, shipmentAssignmentRepository.count());
        assertTrue(shipmentAssignmentRepository.existsById(shipmentAssignment.getId()));

        shipmentService.confirmPickup(assignedShipment.getId());

        Shipment pickedUpShipment = shipmentRepository.findById(shipmentFound.getId()).orElseThrow();

        assertEquals(ShipmentStatus.IN_TRANSIT, pickedUpShipment.getStatus());
        assertTrue(shipmentAssignmentRepository.existsById(shipmentAssignment.getId()));

        shipmentService.confirmDelivery(pickedUpShipment.getId());

        entityManager.flush();
        entityManager.clear();

        Shipment deliveredShipment = shipmentRepository.findById(shipment.getId()).orElseThrow();

        Courier releasedCourier = courierRepository.findById(courier.getId()).orElseThrow();

        Vehicle releasedVehicle = vehicleRepository.findById(vehicle.getId()).orElseThrow();

        ShipmentAssignment completedAssignment = shipmentAssignmentRepository.findById(shipmentAssignment.getId()).orElseThrow();

        assertEquals(ShipmentStatus.DELIVERED, deliveredShipment.getStatus());
        assertTrue(releasedCourier.isAvailable());
        assertTrue(releasedVehicle.isAvailable());
        assertEquals(AssignmentStatus.COMPLETED, completedAssignment.getStatus());
        assertNotNull(completedAssignment.getFinishedAt());
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
