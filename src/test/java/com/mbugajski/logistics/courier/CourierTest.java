package com.mbugajski.logistics.courier;

import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.courier.exception.CourierInvalidStateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CourierTest {

    @Test
    void shouldCreateCourier() {
        Courier courier = new Courier("Listonosz", "Pat", "+48 900 239 145");

        assertEquals("Listonosz", courier.getFirstName());
        assertEquals("Pat", courier.getLastName());
        assertEquals("+48 900 239 145", courier.getPhoneNumber());
        assertTrue(courier.isAvailable());
        assertTrue(courier.isActive());
    }

    @Test
    void shouldThrowWhenFirstNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Courier(" ", "Pat", "+48 900 239 145"));
    }

    @Test
    void shouldThrowWhenFirstNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Courier(null, "Pat", "+48 900 239 145"));
    }

    @Test
    void shouldThrowWhenLastNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Courier("Listonosz", " ", "+48 900 239 145"));
    }

    @Test
    void shouldThrowWhenLastNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Courier("Listonosz", null, "+48 900 239 145"));
    }

    @Test
    void shouldThrowWhenPhoneNumberIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Courier("Listonosz", "Pat", " "));
    }


    @Test
    void shouldThrowWhenPhoneNumberIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Courier("Listonosz", "Pat", null));
    }

    @Test
    void shouldMarkCourierAsBusy() {
        Courier courier = new Courier("Listonosz", "Pat", "+48 900 239 145");

        courier.markAsBusy();

        assertTrue(courier.isActive());
        assertFalse(courier.isAvailable());
    }

    @Test
    void shouldMarkCourierAsAvailable() {
        Courier courier = new Courier("Listonosz", "Pat", "+48 900 239 145");

        courier.markAsBusy();
        courier.markAsAvailable();

        assertTrue(courier.isActive());
        assertTrue(courier.isAvailable());
    }

    @Test
    void shouldThrowWhenMarkingAlreadyBusyCourierAsBusy() {
        Courier courier = new Courier("Listonosz", "Pat", "+48 900 239 145");

        courier.markAsBusy();

        CourierInvalidStateException exception = assertThrows(CourierInvalidStateException.class, courier::markAsBusy);

        assertEquals("Courier must be active and available to mark as busy.", exception.getMessage());
        assertTrue(courier.isActive());
        assertFalse(courier.isAvailable());
    }

    @Test
    void shouldThrowWhenMarkingAvailableCourierAsAvailable() {
        Courier courier = new Courier("Listonosz", "Pat", "+48 900 239 145");

        CourierInvalidStateException exception = assertThrows(CourierInvalidStateException.class, courier::markAsAvailable);

        assertEquals("Courier must be active and busy to mark as available.", exception.getMessage());
        assertTrue(courier.isActive());
        assertTrue(courier.isAvailable());
    }

    @Test
    void shouldDeactivateCourier() {
        Courier courier = new Courier("Listonosz", "Pat", "+48 900 239 145");

        courier.deactivate();

        assertFalse(courier.isActive());
        assertFalse(courier.isAvailable());
    }

    @Test
    void shouldThrowWhenDeactivatingBusyCourier() {
        Courier courier = new Courier("Listonosz", "Pat", "+48 900 239 145");

        courier.markAsBusy();

        CourierInvalidStateException exception = assertThrows(CourierInvalidStateException.class, courier::deactivate);

        assertEquals("Courier must be active and not busy to deactivate.", exception.getMessage());
        assertTrue(courier.isActive());
        assertFalse(courier.isAvailable());
    }

    @Test
    void shouldThrowWhenDeactivatingAlreadyInactiveCourier() {
        Courier courier = new Courier("Listonosz", "Pat", "+48 900 239 145");

        courier.deactivate();

        CourierInvalidStateException exception = assertThrows(CourierInvalidStateException.class, courier::deactivate);

        assertEquals("Courier must be active and not busy to deactivate.", exception.getMessage());
        assertFalse(courier.isActive());
        assertFalse(courier.isAvailable());
    }

    @Test
    void shouldActivateCourier() {
        Courier courier = new Courier("Listonosz", "Pat", "+48 900 239 145");

        courier.deactivate();
        assertFalse(courier.isActive());

        courier.activate();

        assertTrue(courier.isActive());
        assertTrue(courier.isAvailable());
    }

    @Test
    void shouldThrowWhenActivatingAlreadyActiveCourier() {
        Courier courier = new Courier("Listonosz", "Pat", "+48 900 239 145");

        CourierInvalidStateException exception = assertThrows(CourierInvalidStateException.class, courier::activate);

        assertEquals("Only inactive courier can be activated.", exception.getMessage());
        assertTrue(courier.isActive());
        assertTrue(courier.isAvailable());
    }
}
