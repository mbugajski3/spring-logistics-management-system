package com.mbugajski.logistics.customer;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerTest {

    @Test
    void shouldCreateActiveCustomerWithoutDebt() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        assertEquals(1L, customer.getId());
        assertEquals("Franciszek", customer.getFirstName());
        assertEquals("Cyprian", customer.getLastName());
        assertEquals("franciszek@cyprian.com", customer.getEmail());
        assertEquals("+48 777 222 333", customer.getPhoneNumber());
        assertSame(address, customer.getAddress());
        assertEquals(BigDecimal.ZERO, customer.getDebt());
        assertTrue(customer.isActive());
    }

    @Test
    void shouldAddDebt() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        customer.addDebt(new BigDecimal("50.00"));
        customer.addDebt(new BigDecimal("25.50"));

        assertEquals(new BigDecimal("75.50"), customer.getDebt());
    }

    @Test
    void shouldPayPartOfDebt() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        customer.addDebt(new BigDecimal("100.00"));
        customer.payDebt(new BigDecimal("35.50"));

        assertEquals(new BigDecimal("64.50"), customer.getDebt());
    }

    @Test
    void shouldPayEntireDebt() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        customer.addDebt(new BigDecimal("100.00"));
        customer.payDebt(new BigDecimal("100.00"));

        assertEquals(new BigDecimal("0.00"), customer.getDebt());
    }

    @Test
    void shouldDeactivateCustomer() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        customer.deactivate();

        assertFalse(customer.isActive());
    }

    @Test
    void shouldActivateDeactivatedCustomer() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);
        customer.deactivate();

        customer.activate();

        assertTrue(customer.isActive());
    }

    @Test
    void shouldChangeFirstName() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        customer.changeFirstName("Adam  ");

        assertEquals("Adam", customer.getFirstName());
        assertEquals("Cyprian", customer.getLastName());
    }

    @Test
    void shouldRejectBlankFirstName() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        assertThrows(IllegalArgumentException.class, () -> customer.changeFirstName("  "));
        assertEquals("Franciszek", customer.getFirstName());
    }

    @Test
    void shouldRejectNullFirstName() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        assertThrows(IllegalArgumentException.class, () -> customer.changeFirstName(null));
        assertEquals("Franciszek", customer.getFirstName());
    }

    @Test
    void shouldChangeLastName() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        customer.changeLastName("  Nowak");

        assertEquals("Nowak", customer.getLastName());
        assertEquals("Franciszek", customer.getFirstName());
    }

    @Test
    void shouldRejectBlankLastName() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        assertThrows(IllegalArgumentException.class, () -> customer.changeLastName("  "));
        assertEquals("Cyprian", customer.getLastName());
    }

    @Test
    void shouldRejectNullLastName() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        assertThrows(IllegalArgumentException.class, () -> customer.changeLastName(null));
        assertEquals("Cyprian", customer.getLastName());
    }

    @Test
    void shouldChangeEmail() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        customer.changeEmail("  test@example.com");

        assertEquals("test@example.com", customer.getEmail());
        assertEquals("Franciszek", customer.getFirstName());
    }

    @Test
    void shouldRejectBlankEmail() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        assertThrows(IllegalArgumentException.class, () -> customer.changeEmail("  "));
        assertEquals("franciszek@cyprian.com", customer.getEmail());
    }

    @Test
    void shouldRejectNullEmail() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        assertThrows(IllegalArgumentException.class, () -> customer.changeEmail(null));
        assertEquals("franciszek@cyprian.com", customer.getEmail());
    }

    @Test
    void shouldChangePhoneNumber() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        customer.changePhoneNumber("  +48 857 231 423   ");

        assertEquals("+48 857 231 423", customer.getPhoneNumber());
        assertEquals("Franciszek", customer.getFirstName());
    }

    @Test
    void shouldRejectBlankPhoneNumber() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        assertThrows(IllegalArgumentException.class, () -> customer.changePhoneNumber("  "));
        assertEquals("+48 777 222 333", customer.getPhoneNumber());
    }

    @Test
    void shouldRejectNullPhoneNumber() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        assertThrows(IllegalArgumentException.class, () -> customer.changePhoneNumber(null));
        assertEquals("+48 777 222 333", customer.getPhoneNumber());
    }

    @Test
    void shouldChangeAddress() {
        Address oldAddress = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Address newAddress = new Address("Zachodnia", "330", "20", "Kielce", "70-223", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", oldAddress);

        customer.changeAddress(newAddress);

        assertEquals("Zachodnia", customer.getAddress().getStreet());
        assertEquals("330", customer.getAddress().getBuildingNumber());
        assertEquals("20", customer.getAddress().getApartmentNumber());
        assertEquals("Kielce", customer.getAddress().getCity());
        assertEquals("70-223", customer.getAddress().getPostalCode());
        assertEquals("Poland", customer.getAddress().getCountry());

        assertEquals("Franciszek", customer.getFirstName());
        assertSame(newAddress, customer.getAddress());
    }

    @Test
    void shouldRejectNullAddress() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        assertThrows(IllegalArgumentException.class, () -> customer.changeAddress(null));

        assertEquals("Wschodnia", customer.getAddress().getStreet());
        assertEquals("130", customer.getAddress().getBuildingNumber());
        assertEquals("15", customer.getAddress().getApartmentNumber());
        assertEquals("Łódź", customer.getAddress().getCity());
        assertEquals("90-266", customer.getAddress().getPostalCode());
        assertEquals("Poland", customer.getAddress().getCountry());
    }
}
