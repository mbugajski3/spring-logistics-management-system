package com.mbugajski.logistics.assignment;

import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.address.repository.AddressRepository;
import com.mbugajski.logistics.assignment.entity.ShipmentAssignment;
import com.mbugajski.logistics.assignment.repository.ShipmentAssignmentRepository;
import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.courier.repository.CourierRepository;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.customer.repository.CustomerRepository;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.repository.ShipmentRepository;
import com.mbugajski.logistics.vehicle.entity.Vehicle;
import com.mbugajski.logistics.vehicle.entity.VehicleType;
import com.mbugajski.logistics.vehicle.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest
public class ShipmentAssignmentRepositoryTest {

    @Autowired
    private ShipmentAssignmentRepository assignmentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldFindAssignmentByShipmentId() {
        Address customerAddress = new Address("Klientowa", "20", "30", "Opole", "40-211", "Poland");
        addressRepository.saveAndFlush(customerAddress);

        Address pickupAddress = createPickupAddress();
        addressRepository.saveAndFlush(pickupAddress);

        Address deliveryAddress = createDeliveryAddress();
        addressRepository.saveAndFlush(deliveryAddress);

        Customer customer = createCustomer(customerAddress);
        customerRepository.saveAndFlush(customer);

        Shipment shipment = new Shipment(customer, pickupAddress, deliveryAddress, new BigDecimal("6.00"));
        shipmentRepository.saveAndFlush(shipment);

        Courier courier = createCourier();
        courierRepository.saveAndFlush(courier);

        Vehicle vehicle = createVehicle();
        vehicleRepository.saveAndFlush(vehicle);

        ShipmentAssignment assignment = new ShipmentAssignment(shipment, courier, vehicle);
        assignmentRepository.saveAndFlush(assignment);

        ShipmentAssignment shipmentAssignment = assignmentRepository.findByShipmentId(shipment.getId()).orElseThrow();

        assertEquals(shipment.getId(), shipmentAssignment.getShipment().getId());
        assertEquals(courier.getId(), shipmentAssignment.getCourier().getId());
        assertEquals(vehicle.getId(), shipmentAssignment.getVehicle().getId());
    }

    @Test
    void shouldReturnEmptyWhenAssignmentDoesNotExist() {
        Optional<ShipmentAssignment> foundAssignment = assignmentRepository.findByShipmentId(1L);

        assertTrue(foundAssignment.isEmpty());
    }

    public Customer createCustomer(Address address) {
        return new Customer("Adrian", "Nowak", "adrian@nowak.com","+48 643 234 532", address);
    }

    public Address createPickupAddress() {
        return new Address("Odbiorowa", "35", "2", "Kielce", "70-213", "Poland");
    }

    public Address createDeliveryAddress() {
        return new Address("Wysyłkowa", "10", "3", "Warszawa", "10-231", "Poland");
    }

    public Courier createCourier() {
        return new Courier("Postman", "Pat", "+48 532 235 125");
    }

    public Vehicle createVehicle() {
        return new Vehicle("Ford", "Ducato", "GD 2314D", VehicleType.VAN, new BigDecimal("600.00"));
    }
}
