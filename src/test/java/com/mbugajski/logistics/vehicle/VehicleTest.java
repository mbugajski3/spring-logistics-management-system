package com.mbugajski.logistics.vehicle;

import com.mbugajski.logistics.vehicle.entity.Vehicle;
import com.mbugajski.logistics.vehicle.entity.VehicleType;
import com.mbugajski.logistics.vehicle.exception.VehicleInvalidStateException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class VehicleTest {

    @Test
    void shouldCreateVehicle() {
        Vehicle vehicle = new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30"));

        assertEquals("Ford", vehicle.getBrand());
        assertEquals("Transit", vehicle.getModel());
        assertEquals("GD 0406H", vehicle.getRegistrationNumber());
        assertEquals(VehicleType.VAN, vehicle.getVehicleType());
        assertEquals(new BigDecimal("30"), vehicle.getMaximumLoad());
        assertTrue(vehicle.isActive());
        assertTrue(vehicle.isAvailable());
    }

    @Test
    void shouldThrowWhenBrandIsBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Vehicle(" ", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30")));

        assertEquals("Brand cannot be empty or null.", exception.getMessage());
    }

    @Test
    void shouldThrowWhenBrandIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Vehicle(null, "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30")));

        assertEquals("Brand cannot be empty or null.", exception.getMessage());
    }

    @Test
    void shouldThrowWhenModelIsBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Vehicle("Ford", " ", "GD 0406H", VehicleType.VAN, new BigDecimal("30")));

        assertEquals("Model cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shouldThrowWhenModelIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Vehicle("Ford", null, "GD 0406H", VehicleType.VAN, new BigDecimal("30")));

        assertEquals("Model cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shouldThrowWhenRegistrationNumberIsBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Vehicle("Ford", "Transit", " ", VehicleType.VAN, new BigDecimal("30")));

        assertEquals("Registration number cannot be null or blank.", exception.getMessage());
    }

    @Test
    void shouldThrowWhenRegistrationNumberIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Vehicle("Ford", "Transit", null, VehicleType.VAN, new BigDecimal("30")));

        assertEquals("Registration number cannot be null or blank.", exception.getMessage());
    }

    @Test
    void shouldThrowWhenVehicleTypeIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Vehicle("Ford", "Transit", "GD 0406H", null, new BigDecimal("30")));

        assertEquals("Vehicle type cannot be null.", exception.getMessage());
    }

    @Test
    void shouldThrowWhenMaxLoadIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, null));

        assertEquals("Maximum load cannot be null.", exception.getMessage());
    }

    @Test
    void shouldThrowWhenMaxLoadIsZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("0")));

        assertEquals("Maximum load cannot be 0 or below.", exception.getMessage());
    }

    @Test
    void shouldThrowWhenMaxLoadIsBelowZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("-5")));

        assertEquals("Maximum load cannot be 0 or below.", exception.getMessage());
    }

    @Test
    void shouldThrowWhenMaxLoadIsAboveLimit() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("1600.01")));

        assertEquals("Maximum load cannot be over 1600 kg.", exception.getMessage());
    }

    @Test
    void shouldMarkAsBusy() {
        Vehicle vehicle = new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30"));

        vehicle.markAsBusy();

        assertTrue(vehicle.isActive());
        assertFalse(vehicle.isAvailable());
    }

    @Test
    void shouldMarkAsAvailable() {
        Vehicle vehicle = new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30"));

        vehicle.markAsBusy();
        vehicle.markAsAvailable();

        assertTrue(vehicle.isAvailable());
        assertTrue(vehicle.isActive());
    }

    @Test
    void shouldDeactivate() {
        Vehicle vehicle = new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30"));

        vehicle.deactivate();

        assertFalse(vehicle.isActive());
        assertFalse(vehicle.isAvailable());
    }

    @Test
    void shouldActivate() {
        Vehicle vehicle = new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30"));

        vehicle.deactivate();
        vehicle.activate();

        assertTrue(vehicle.isActive());
        assertTrue(vehicle.isAvailable());
    }

    @Test
    void shouldThrowWhenMarkingAlreadyBusyVehicleAsBusy() {
        Vehicle vehicle = new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30"));

        vehicle.markAsBusy();

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class, vehicle::markAsBusy);

        assertEquals("Vehicle must be active and available to mark as busy.", exception.getMessage());
        assertTrue(vehicle.isActive());
        assertFalse(vehicle.isAvailable());
    }

    @Test
    void shouldThrowWhenMarkingAvailableVehicleAsAvailable() {
        Vehicle vehicle = new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30"));

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class, vehicle::markAsAvailable);

        assertEquals("Vehicle must be active and busy to mark as available.", exception.getMessage());
        assertTrue(vehicle.isActive());
        assertTrue(vehicle.isAvailable());
    }

    @Test
    void shouldThrowWhenDeactivatingBusyVehicle() {
        Vehicle vehicle = new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30"));

        vehicle.markAsBusy();
        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class, vehicle::deactivate);

        assertEquals("Only an active and available vehicle can be deactivated.", exception.getMessage());
        assertTrue(vehicle.isActive());
        assertFalse(vehicle.isAvailable());
    }

    @Test
    void shouldThrowWhenDeactivatingAlreadyInactiveVehicle() {
        Vehicle vehicle = new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30"));

        vehicle.deactivate();
        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class, vehicle::deactivate);

        assertEquals("Only an active and available vehicle can be deactivated.", exception.getMessage());
        assertFalse(vehicle.isActive());
        assertFalse(vehicle.isAvailable());
    }

    @Test
    void shouldThrowWhenActivatingAlreadyActiveVehicle() {
        Vehicle vehicle = new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30"));

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class, vehicle::activate);

        assertEquals("Only inactive vehicle can be activated.", exception.getMessage());
        assertTrue(vehicle.isActive());
        assertTrue(vehicle.isAvailable());
    }
}
