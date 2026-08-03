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
        assertEquals("Franciszek Cyprian", customer.getName());
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
}
