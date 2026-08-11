package com.mbugajski.logistics.shipment;

import com.mbugajski.logistics.address.dto.request.CreateAddressRequest;
import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.shipment.dto.response.ShipmentResponse;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.mapper.ShipmentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;


import java.math.BigDecimal;

public class ShipmentMapperTest {

    @Test
    void shouldMapShipmentToShipmentResponse() {
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 782 230 124",
                createAddress()
        );

        Address pickupAddress = mapToAddress(createPickupAddressRequest());
        Address deliveryAddress = mapToAddress(createDeliveryAddressRequest());

        Shipment shipment = new Shipment(customer, pickupAddress, deliveryAddress, new BigDecimal("7.00"));

        ReflectionTestUtils.setField(customer, "id", 1L);
        ReflectionTestUtils.setField(shipment, "id", 1L);

        ShipmentResponse response = ShipmentMapper.toResponse(shipment);

        assertEquals(shipment.getId(), response.getId());
        assertEquals(customer.getId(), response.getCustomerId());
        assertEquals(customer.getFirstName(), response.getCustomerFirstName());
        assertEquals(customer.getLastName(), response.getCustomerLastName());
        assertEquals(pickupAddress.getStreet(), response.getPickupAddress().getStreet());
        assertEquals(deliveryAddress.getStreet(), response.getDeliveryAddress().getStreet());
        assertEquals(shipment.getWeight(), response.getWeight());
        assertEquals(shipment.getPrice(), response.getPrice());
        assertEquals(shipment.getStatus(), response.getStatus());
        assertEquals(shipment.getCreatedAt(), response.getCreatedAt());
    }

    private CreateAddressRequest createPickupAddressRequest() {
        CreateAddressRequest request = new CreateAddressRequest();

        request.setStreet("Odbiorowa");
        request.setBuildingNumber("10");
        request.setApartmentNumber("2");
        request.setCity("Odbiór");
        request.setPostalCode("10-120");
        request.setCountry("Poland");

        return request;
    }

    private CreateAddressRequest createDeliveryAddressRequest() {
        CreateAddressRequest request = new CreateAddressRequest();

        request.setStreet("Wysyłkowa");
        request.setBuildingNumber("5");
        request.setApartmentNumber("3");
        request.setCity("Wysyłka");
        request.setPostalCode("90-192");
        request.setCountry("Poland");

        return request;
    }

    private Address createAddress() {
        return new Address("Klientowa", "12", "23", "Poznań", "90-231", "Poland");
    }

    private Address mapToAddress(CreateAddressRequest request) {
        return new Address(
                request.getStreet(),
                request.getBuildingNumber(),
                request.getApartmentNumber(),
                request.getCity(),
                request.getPostalCode(),
                request.getCountry()
        );
    }

    private Shipment createShipment(Customer customer) {
        CreateAddressRequest requestPickupAddress = createPickupAddressRequest();
        CreateAddressRequest requestDeliveryAddress = createDeliveryAddressRequest();

        return new Shipment(customer, mapToAddress(requestPickupAddress), mapToAddress(requestDeliveryAddress), new BigDecimal("12.00"));

    }
}
