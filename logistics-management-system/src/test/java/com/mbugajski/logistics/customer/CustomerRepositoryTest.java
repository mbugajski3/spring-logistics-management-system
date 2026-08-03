package com.mbugajski.logistics.customer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerRepositoryTest {

    @Test
    void shouldCreateAndFindCustomerById() {
        CustomerRepository testRepository = new CustomerRepository();
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        testRepository.create(customer);

        Optional<Customer> foundCustomer = testRepository.findById(1L);

        assertTrue(foundCustomer.isPresent());
        assertSame(customer, foundCustomer.get());
    }

    @Test
    void shouldReturnEmptyOptionalWhenCustomerDoesNotExist() {
        CustomerRepository testRepository = new CustomerRepository();

        Optional<Customer> foundCustomer = testRepository.findById(1L);

        assertTrue(foundCustomer.isEmpty());

    }

    @Test
    void shouldReturnAllCustomers() {
        CustomerRepository testRepository = new CustomerRepository();
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer1 = new Customer(1L, "Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);
        Customer customer2 = new Customer(2L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        testRepository.create(customer1);
        testRepository.create(customer2);
        List<Customer> foundCustomers = testRepository.findAll();

        assertEquals(2, foundCustomers.size());
        assertTrue(foundCustomers.contains(customer1));
        assertTrue(foundCustomers.contains(customer2));
    }

    @Test
    void shouldRecognizeExistingEmailIgnoringCaseAndSpaces() {
        CustomerRepository testRepository = new CustomerRepository();
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer1 = new Customer(1L, "Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        testRepository.create(customer1);

        assertTrue(testRepository.existsByEmail("adrian@nowak.com"));
        assertTrue(testRepository.existsByEmail("  ADRIAN@NOWAK.COM  "));
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        CustomerRepository testRepository = new CustomerRepository();
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer1 = new Customer(1L, "Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        testRepository.create(customer1);

        assertFalse(testRepository.existsByEmail("unknown@mail.com"));
    }

    @Test
    void shouldDeleteExistingCustomer() {
        CustomerRepository testRepository = new CustomerRepository();
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer1 = new Customer(1L, "Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        testRepository.create(customer1);
        boolean deleted = testRepository.deleteById(1L);

        assertTrue(deleted);
        assertTrue(testRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistingCustomer() {
        CustomerRepository testRepository = new CustomerRepository();

        boolean deleted = testRepository.deleteById(1L);

        assertFalse(deleted);
    }

    @Test
    void shouldReturnIndependentListFromFindAll() {
        CustomerRepository testRepository = new CustomerRepository();
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer1 = new Customer(1L, "Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        testRepository.create(customer1);
        List<Customer> foundCustomers = testRepository.findAll();
        foundCustomers.clear();

        assertEquals(1, testRepository.findAll().size());
    }

    @Test
    void shouldRejectDuplicateCustomerId() {
        CustomerRepository testRepository = new CustomerRepository();
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer1 = new Customer(1L, "Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);
        Customer customer2 = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        testRepository.create(customer1);

        assertThrows(IllegalArgumentException.class, () -> testRepository.create(customer2));
    }

    @Test
    void shouldRejectNullCustomer() {
        CustomerRepository testRepository = new CustomerRepository();

        assertThrows(IllegalArgumentException.class, () -> testRepository.create(null));
    }

    @Test
    void shouldRejectNonPositiveCustomerIdWhenFinding() {
        CustomerRepository testRepository = new CustomerRepository();

        assertThrows(IllegalArgumentException.class, () -> testRepository.findById(0L));
        assertThrows(IllegalArgumentException.class, () -> testRepository.findById(-1L));
    }

    @Test
    void shouldRejectNonPositiveCustomerIdWhenDeleting() {
        CustomerRepository testRepository = new CustomerRepository();

        assertThrows(IllegalArgumentException.class, () -> testRepository.deleteById(0L));
        assertThrows(IllegalArgumentException.class, () -> testRepository.deleteById(-1L));
    }

    @Test
    void shouldRejectNullOrBlankEmail() {
        CustomerRepository testRepository = new CustomerRepository();

        assertThrows(IllegalArgumentException.class, () -> testRepository.existsByEmail(null));
        assertThrows(IllegalArgumentException.class, () -> testRepository.existsByEmail("   "));
    }
}
