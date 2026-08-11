package com.mbugajski.logistics.customer;

import com.mbugajski.logistics.address.entity.Address;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void shouldCreateAddressWithApartment() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");

        assertEquals("Wschodnia", address.getStreet());
        assertEquals("130", address.getBuildingNumber());
        assertEquals("15", address.getApartmentNumber());
        assertEquals("Łódź", address.getCity());
        assertEquals("90-266", address.getPostalCode());
        assertEquals("Poland", address.getCountry());
    }

    @Test
    void shouldCreateAddressWithoutApartment() {
        Address address = new Address("Osobna", "40", null, "Kielce", "25-034", "Poland");
        String addressText = address.toString();

        assertEquals("Osobna", address.getStreet());
        assertEquals("40", address.getBuildingNumber());
        assertNull(address.getApartmentNumber());
        assertEquals("Kielce", address.getCity());
        assertEquals("25-034", address.getPostalCode());
        assertEquals("Poland", address.getCountry());

        assertFalse(addressText.contains("null"));
        assertFalse(addressText.contains("m."));
        assertTrue(addressText.contains("Osobna 40"));
    }
}
