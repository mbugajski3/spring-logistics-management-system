package com.mbugajski.logistics.shipment;

import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.address.repository.AddressRepository;
import com.mbugajski.logistics.customer.repository.CustomerRepository;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.repository.ShipmentRepository;
import com.mbugajski.logistics.shipment.entity.ShipmentStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ShipmentRepositoryTest {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveShipment() {
        Address customerAddress = addressRepository.save(createAddress());
        Customer customer = customerRepository.save(createCustomer(customerAddress));
        Address pickupAddress = addressRepository.save(createPickupAddress());
        Address deliveryAddress = addressRepository.save(createDeliveryAddress());

        Shipment shipment = new Shipment(customer, pickupAddress, deliveryAddress, new BigDecimal("7.00"));
        Shipment savedShipment = shipmentRepository.save(shipment);

        Long shipmentId = savedShipment.getId();

        shipmentRepository.flush();
        entityManager.clear();

        Shipment foundShipment = shipmentRepository.findById(shipmentId).orElseThrow();

        assertNotNull(savedShipment.getId());
        assertEquals("Adrian", foundShipment.getCustomer().getFirstName());
        assertEquals("Odbiorowa", foundShipment.getPickupAddress().getStreet());
        assertEquals("Wysyłkowa", foundShipment.getDeliveryAddress().getStreet());
        assertEquals(new BigDecimal("7.00"), foundShipment.getWeight());
        assertEquals(new BigDecimal("25.00"), foundShipment.getPrice());
        assertEquals(ShipmentStatus.CREATED, foundShipment.getStatus());
        assertNotNull(foundShipment.getCreatedAt());
    }

    @Test
    void shouldPersistShipmentStatusChange() {
        Address customerAddress = addressRepository.save(createAddress());
        Customer customer = customerRepository.save(createCustomer(customerAddress));
        Address pickupAddress = addressRepository.save(createPickupAddress());
        Address deliveryAddress = addressRepository.save(createDeliveryAddress());

        Shipment shipment = new Shipment(customer, pickupAddress, deliveryAddress, new BigDecimal("7.00"));
        Shipment savedShipment = shipmentRepository.save(shipment);
        savedShipment.markAsReadyForPickup();

        Long shipmentId = savedShipment.getId();

        shipmentRepository.flush();
        entityManager.clear();

        Shipment foundShipment = shipmentRepository
                .findById(shipmentId)
                .orElseThrow();

        assertEquals(ShipmentStatus.READY_FOR_PICKUP, foundShipment.getStatus());
    }

    public Address createAddress() {
        return new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
    }

    public Customer createCustomer(Address address) {
        return new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 605 230 140",
                address
        );
    }

    public Address createPickupAddress() {
        return new Address(
                "Odbiorowa",
                "10",
                "2",
                "Odbiór",
                "10-120",
                "Poland"
        );
    }

    public Address createDeliveryAddress() {
        return new Address(
                "Wysyłkowa",
                "5",
                "3",
                "Wysyłka",
                "90-192",
                "Poland"
        );
    }
}
