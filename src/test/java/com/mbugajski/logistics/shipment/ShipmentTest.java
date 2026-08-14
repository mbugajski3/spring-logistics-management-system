package com.mbugajski.logistics.shipment;

import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.exception.*;
import org.junit.jupiter.api.Test;
import com.mbugajski.logistics.shipment.entity.ShipmentStatus;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


public class ShipmentTest {

    @Test
    void shouldCreateValidShipment() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        Shipment shipment = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("14.00")
        );

        assertEquals(new BigDecimal("35.00"), shipment.getPrice());
        assertEquals("Adrian", shipment.getCustomer().getFirstName());
        assertEquals("Zachodnia", shipment.getPickupAddress().getStreet());
        assertEquals("Wschodnia", shipment.getDeliveryAddress().getStreet());
        assertEquals(new BigDecimal("14.00"), shipment.getWeight());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
        assertNotNull(shipment.getCreatedAt());
    }

    @Test
    void shouldRejectShipmentWithNullCustomer() {
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        ShipmentNullCustomerException exception = assertThrows(ShipmentNullCustomerException.class, () -> new Shipment(null, pickupAddress, deliveryAddress, new BigDecimal("5.00")));

        assertEquals("Customer cannot be null.", exception.getMessage());
    }

    @Test
    void shouldRejectShipmentWithNullPickupAddress() {
        Customer customer = createCustomer();
        Address deliveryAddress = createDeliveryAddress();

        ShipmentNullAddressException exception = assertThrows(ShipmentNullAddressException.class, () -> new Shipment(customer, null, deliveryAddress, new BigDecimal("5.00")));

        assertEquals("Pickup address cannot be null.", exception.getMessage());
    }

    @Test
    void shouldRejectShipmentWithNullDeliveryAddress() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();

        ShipmentNullAddressException exception = assertThrows(ShipmentNullAddressException.class, () -> new Shipment(customer, pickupAddress, null, new BigDecimal("5.00")));

        assertEquals("Delivery address cannot be null.", exception.getMessage());
    }

    @Test
    void shouldRejectShipmentWithZeroWeight() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        ShipmentInvalidWeightException exception = assertThrows(
                ShipmentInvalidWeightException.class, () ->
                        new Shipment(
                                customer,
                                pickupAddress,
                                deliveryAddress,
                                new BigDecimal("0")
                        )
        );

        assertEquals("A shipment weight cannot be zero or negative", exception.getMessage());
    }

    @Test
    void shouldRejectShipmentWithNegativeWeight() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        ShipmentInvalidWeightException exception = assertThrows(
                ShipmentInvalidWeightException.class, () ->
                        new Shipment(
                                customer,
                                pickupAddress,
                                deliveryAddress,
                                new BigDecimal("-1")
                        )
        );

        assertEquals("A shipment weight cannot be zero or negative", exception.getMessage());
    }

    @Test
    void shouldRejectShipmentWithNullWeight() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        ShipmentNullWeightException exception = assertThrows(
                ShipmentNullWeightException.class, () ->
                        new Shipment(
                                customer,
                                pickupAddress,
                                deliveryAddress,
                                null
                        )
        );

        assertEquals("Shipment weight cannot be null.", exception.getMessage());
    }

    @Test
    void shouldAllowShipmentWithWeightExactly20Kg() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        assertDoesNotThrow(() -> new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("20.00")
        )
        );
    }

    @Test
    void shouldRejectShipmentWithWeightAbove20Kg() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        ShipmentOverweightException exception = assertThrows(
                ShipmentOverweightException.class, () ->
                        new Shipment(
                                customer,
                                pickupAddress,
                                deliveryAddress,
                                new BigDecimal("20.01")
                        )
        );

        assertEquals("Shipment cannot be created for weight 20.01 kg.", exception.getMessage());
    }

    @Test
    void shouldCalculatePriceForShipmentUpTo1Kg() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        Shipment shipment1 = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("0.01")
        );

        Shipment shipment2 = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("1.00")
        );

        assertEquals(new BigDecimal("12.00"), shipment1.getPrice());
        assertEquals(new BigDecimal("12.00"), shipment2.getPrice());
    }

    @Test
    void shouldCalculatePriceForShipmentBetween1and5Kg() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        Shipment shipment1 = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("1.01")
        );

        Shipment shipment2 = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("5.00")
        );

        assertEquals(new BigDecimal("17.00"), shipment1.getPrice());
        assertEquals(new BigDecimal("17.00"), shipment2.getPrice());
    }

    @Test
    void shouldCalculatePriceForShipmentBetween5And10Kg() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        Shipment shipment1 = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("5.01")
        );

        Shipment shipment2 = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("10.00")
        );

        assertEquals(new BigDecimal("25.00"), shipment1.getPrice());
        assertEquals(new BigDecimal("25.00"), shipment2.getPrice());
    }

    @Test
    void shouldCalculatePriceForShipmentBetween10And20Kg() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        Shipment shipment1 = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("10.01")
        );

        Shipment shipment2 = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("20.00")
        );

        assertEquals(new BigDecimal("35.00"), shipment1.getPrice());
        assertEquals(new BigDecimal("35.00"), shipment2.getPrice());
    }

    @Test
    void shouldRejectMarkingNonCreatedShipmentAsReadyForPickup() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        Shipment shipment = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("10.01")
        );
        shipment.markAsReadyForPickup();

        ShipmentInvalidStatusException exception = assertThrows(ShipmentInvalidStatusException.class, shipment::markAsReadyForPickup);

        assertEquals("Only a shipment with status 'CREATED' can be marked as 'READY_FOR_PICKUP'.", exception.getMessage());
        assertEquals(ShipmentStatus.READY_FOR_PICKUP, shipment.getStatus());
    }

    @Test
    void shouldMarkReadyForPickupShipmentAsInTransit() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        Shipment shipment = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("10.01")
        );
        shipment.markAsReadyForPickup();
        shipment.markAsInTransit();

        assertEquals(ShipmentStatus.IN_TRANSIT, shipment.getStatus());
    }

    @Test
    void shouldRejectMarkingNonReadyForPickupShipmentAsInTransit() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        Shipment shipment = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("10.01")
        );

        ShipmentInvalidStatusException exception = assertThrows(ShipmentInvalidStatusException.class, shipment::markAsInTransit);

        assertEquals("Only a shipment with status 'READY_FOR_PICKUP' can be marked as 'IN_TRANSIT'.", exception.getMessage());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
    }

    @Test
    void shouldMarkInTransitShipmentAsDelivered() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        Shipment shipment = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("10.01")
        );
        shipment.markAsReadyForPickup();
        shipment.markAsInTransit();
        shipment.markAsDelivered();

        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
    }

    @Test
    void shouldRejectMarkingNonInTransitShipmentAsDelivered() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        Shipment shipment = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("10.01")
        );

        ShipmentInvalidStatusException exception = assertThrows(ShipmentInvalidStatusException.class, shipment::markAsDelivered);

        assertEquals("Only a shipment with status 'IN_TRANSIT' can be marked as 'DELIVERED'.", exception.getMessage());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
    }

    @Test
    void shouldCancelCreatedShipment() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        Shipment shipment = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("10.01")
        );
        shipment.markAsCancelled();

        assertEquals(ShipmentStatus.CANCELLED, shipment.getStatus());
    }

    @Test
    void shouldCancelReadyForPickupShipment() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        Shipment shipment = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("10.01")
        );
        shipment.markAsReadyForPickup();
        shipment.markAsCancelled();

        assertEquals(ShipmentStatus.CANCELLED, shipment.getStatus());
    }

    @Test
    void shouldRejectCancellingInTransitShipment() {
        Customer customer = createCustomer();
        Address pickupAddress = createPickupAddress();
        Address deliveryAddress = createDeliveryAddress();

        Shipment shipment = new Shipment(
                customer,
                pickupAddress,
                deliveryAddress,
                new BigDecimal("10.01")
        );
        shipment.markAsReadyForPickup();
        shipment.markAsInTransit();

        ShipmentInvalidStatusException exception = assertThrows(ShipmentInvalidStatusException.class, shipment::markAsCancelled);

        assertEquals("Only a shipment with status 'CREATED' or 'READY_FOR_PICKUP' can be cancelled.", exception.getMessage());
        assertEquals(ShipmentStatus.IN_TRANSIT, shipment.getStatus());
    }

    public Customer createCustomer() {
        Address address = new Address("Zachodnia", "15", "20", "Kielce", "80-234", "Poland");

        return new Customer("Adrian", "Nowak", "adrian@nowak.com", "+48 723 732 145", address);
    }

    public Address createPickupAddress() {
        return new Address("Zachodnia", "10", "1", "Warszawa", "10-321", "Poland");
    }

    public Address createDeliveryAddress() {
        return new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
    }
}
