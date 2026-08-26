package com.mbugajski.logistics.assignment;

import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.assignment.entity.AssignmentStatus;
import com.mbugajski.logistics.assignment.entity.ShipmentAssignment;
import com.mbugajski.logistics.assignment.exception.AssignmentInvalidStateException;
import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.vehicle.entity.Vehicle;
import com.mbugajski.logistics.vehicle.entity.VehicleType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ShipmentAssignmentTest {

    @Test
    void shouldAssignShipment() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);

        assertSame(shipment, shipmentAssignment.getShipment());
        assertSame(courier, shipmentAssignment.getCourier());
        assertSame(vehicle, shipmentAssignment.getVehicle());
    }

    @Test
    void shouldThrowWhenShipmentIsNull() {
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> new ShipmentAssignment(null, courier, vehicle));

        assertEquals("Shipment cannot be null.", exception.getMessage());
    }

    @Test
    void shouldThrowWhenCourierIsNull() {
        Shipment shipment = createShipment();
        Vehicle vehicle = createVehicle();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> new ShipmentAssignment(shipment, null, vehicle));

        assertEquals("Courier cannot be null.", exception.getMessage());
    }

    @Test
    void shouldThrowWhenVehicleIsNull() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> new ShipmentAssignment(shipment, courier, null));

        assertEquals("Vehicle cannot be null.", exception.getMessage());
    }

    @Test
    void newAssignmentShouldBeActive() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);

        assertEquals(AssignmentStatus.ACTIVE, shipmentAssignment.getStatus());
    }

    @Test
    void newAssignmentShouldHaveAssignedAt() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);

        assertNotNull(shipmentAssignment.getAssignedAt());
    }

    @Test
    void newAssignmentShouldNotHaveFinishedAt() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);

        assertNull(shipmentAssignment.getFinishedAt());
    }

    @Test
    void completeShouldChangeStatusToCompleted() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);
        shipmentAssignment.complete();

        assertEquals(AssignmentStatus.COMPLETED, shipmentAssignment.getStatus());
    }

    @Test
    void completeShouldSetFinishedAt() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);
        shipmentAssignment.complete();

        assertNotNull(shipmentAssignment.getFinishedAt());
    }

    @Test
    void cancelShouldChangeStatusToCancelled() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);
        shipmentAssignment.cancel();

        assertEquals(AssignmentStatus.CANCELLED, shipmentAssignment.getStatus());
    }

    @Test
    void cancelShouldSetFinishedAt() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);
        shipmentAssignment.cancel();

        assertNotNull(shipmentAssignment.getFinishedAt());
    }

    @Test
    void shouldNotCompleteCompletedAssignment() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);
        shipmentAssignment.complete();

        AssignmentInvalidStateException exception = assertThrows(AssignmentInvalidStateException.class, shipmentAssignment::complete);

        assertEquals("Status must be active to complete.", exception.getMessage());
        assertEquals(AssignmentStatus.COMPLETED, shipmentAssignment.getStatus());
    }

    @Test
    void shouldNotCancelCancelledAssignment() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);
        shipmentAssignment.cancel();

        AssignmentInvalidStateException exception = assertThrows(AssignmentInvalidStateException.class, shipmentAssignment::cancel);

        assertEquals("Status must be active to cancel.", exception.getMessage());
        assertEquals(AssignmentStatus.CANCELLED, shipmentAssignment.getStatus());
    }

    @Test
    void shouldNotCancelCompletedAssignment() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);
        shipmentAssignment.complete();

        AssignmentInvalidStateException exception = assertThrows(AssignmentInvalidStateException.class, shipmentAssignment::cancel);

        assertEquals("Status must be active to cancel.", exception.getMessage());
        assertEquals(AssignmentStatus.COMPLETED, shipmentAssignment.getStatus());
    }

    @Test
    void shouldNotCompleteCancelledAssignment() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);
        shipmentAssignment.cancel();

        AssignmentInvalidStateException exception = assertThrows(AssignmentInvalidStateException.class, shipmentAssignment::complete);

        assertEquals("Status must be active to complete.", exception.getMessage());
        assertEquals(AssignmentStatus.CANCELLED, shipmentAssignment.getStatus());
    }

    private Shipment createShipment() {
        Address customerAddress = new Address("Adrianowa", "20", "14", "Kielce", "50-231", "Poland");
        Address deliveryAddress = new Address("Wysyłkowa", "13", "3", "Krakow", "60-123", "Poland");

        Customer customer = new Customer("Adrian", "Nowak", "adrian@nowak.com", "+48 553 214 532", customerAddress);

        return new Shipment(customer, customerAddress, deliveryAddress, new BigDecimal("5.00"));
    }

    private Courier createCourier() {
        return new Courier("Postman", "Pat", "+48 999 234 523");
    }

    private Vehicle createVehicle() {
        return new Vehicle("Ford", "Transport", "GD 9231L", VehicleType.VAN, new BigDecimal("700.00"));
    }
}
