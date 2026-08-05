package com.mbugajski.logistics.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerRepositoryTest {

    private CustomerRepository testRepository;

    @BeforeEach
    void setUp() {
        testRepository = new CustomerRepository();
    }

    @Test
    void shouldCreateAndFindCustomerById() {
        Address address = createValidAddress();
        Customer customer = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        assertEquals(1L, customer.getId());

        Optional<Customer> foundCustomer =
                testRepository.findById(customer.getId());

        assertTrue(foundCustomer.isPresent());
        assertSame(customer, foundCustomer.get());
    }

    @Test
    void shouldReturnEmptyOptionalWhenCustomerDoesNotExist() {
        Optional<Customer> foundCustomer = testRepository.findById(1L);

        assertTrue(foundCustomer.isEmpty());
    }

    @Test
    void shouldReturnAllCustomers() {
        Address address = createValidAddress();
        Customer customer1 = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);
        Customer customer2 = testRepository.create("Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        List<Customer> foundCustomers = testRepository.findAll();

        assertEquals(2, foundCustomers.size());
        assertTrue(foundCustomers.contains(customer1));
        assertTrue(foundCustomers.contains(customer2));
    }

    @Test
    void shouldRecognizeExistingEmailIgnoringCaseAndSpaces() {
        Address address = createValidAddress();

        testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        assertTrue(testRepository.existsByEmail("adrian@nowak.com"));
        assertTrue(testRepository.existsByEmail("  ADRIAN@NOWAK.COM  "));
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        Address address = createValidAddress();
        testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        assertFalse(testRepository.existsByEmail("unknown@mail.com"));
    }

    @Test
    void shouldDeleteExistingCustomer() {
        Address address = createValidAddress();
        Customer customer = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        boolean deleted = testRepository.deleteById(customer.getId());

        assertTrue(deleted);
        assertTrue(testRepository.findById(customer.getId()).isEmpty());
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistingCustomer() {
        boolean deleted = testRepository.deleteById(1L);

        assertFalse(deleted);
    }

    @Test
    void shouldReturnIndependentListFromFindAll() {
        Address address = createValidAddress();
        testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        List<Customer> foundCustomers = testRepository.findAll();
        foundCustomers.clear();

        assertEquals(1, testRepository.findAll().size());
    }

    @Test
    void shouldGenerateUniqueCustomerIds() {
        Address address = createValidAddress();
        Customer customer1 = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);
        Customer customer2 = testRepository.create("Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        assertNotEquals(customer1.getId(), customer2.getId());
    }

    @Test
    void shouldRejectNonPositiveCustomerIdWhenFinding() {
        assertThrows(IllegalArgumentException.class, () -> testRepository.findById(0L));
        assertThrows(IllegalArgumentException.class, () -> testRepository.findById(-1L));
    }

    @Test
    void shouldRejectNonPositiveCustomerIdWhenDeleting() {
        assertThrows(IllegalArgumentException.class, () -> testRepository.deleteById(0L));
        assertThrows(IllegalArgumentException.class, () -> testRepository.deleteById(-1L));
    }

    @Test
    void shouldRejectNullOrBlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> testRepository.existsByEmail(null));
        assertThrows(IllegalArgumentException.class, () -> testRepository.existsByEmail("   "));
    }

    private Address createValidAddress() {
        return new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
    }
}
