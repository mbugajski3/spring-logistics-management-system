package com.mbugajski.logistics.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;


    @Test
    void shouldCreateAndFindCustomerById() {
        Address address = createValidAddress();
        Address savedAddress = addressRepository.save(address);

        Customer savedCustomer = customerRepository.save(
                new Customer(
                        "Adrian",
                        "Nowak",
                        "adrian@nowak.com",
                        "+48 699 300 299",
                        savedAddress
                )
        );

        Optional<Customer> found = customerRepository.findById(savedCustomer.getId());

        Customer foundCustomer = found.orElseThrow();

        assertEquals("Adrian", foundCustomer.getFirstName());
        assertEquals("Wschodnia", foundCustomer.getAddress().getStreet());
    }

    @Test
    void shouldReturnEmptyOptionalWhenCustomerDoesNotExist() {
        Optional<Customer> foundCustomer = customerRepository.findById(9999999L);

        assertTrue(foundCustomer.isEmpty());
    }

    @Test
    void shouldReturnAllCustomers() {
        Address address = createValidAddress();
        Address savedAddress = addressRepository.save(address);

        Customer savedCustomer1 = customerRepository.save(
                new Customer(
                        "Adrian",
                        "Nowak",
                        "adrian@nowak.com",
                        "+48 699 300 299",
                        savedAddress
                )
        );

        Customer savedCustomer2 = customerRepository.save(
                new Customer(
                        "Franciszek",
                        "Cyprian",
                        "franciszek@cyprian.com",
                        "+48 777 222 333",
                        savedAddress
                )
        );

        List<Customer> foundCustomers = customerRepository.findAll();

        assertEquals(2, foundCustomers.size());

        assertTrue(foundCustomers.stream()
                .anyMatch(customer ->
                        customer.getId().equals(savedCustomer1.getId())));

        assertTrue(foundCustomers.stream()
                .anyMatch(customer ->
                        customer.getId().equals(savedCustomer2.getId())));

        assertTrue(foundCustomers.stream()
                .anyMatch(customer ->
                        customer.getEmail().equals(savedCustomer1.getEmail())));

        assertTrue(foundCustomers.stream()
                .anyMatch(customer ->
                        customer.getEmail().equals(savedCustomer2.getEmail())));
    }

    @Test
    void shouldMatchOnlyExactEmail() {
        Address address = createValidAddress();
        Address savedAddress = addressRepository.save(address);

        customerRepository.save(
                new Customer(
                        "Adrian",
                        "Nowak",
                        "adrian@nowak.com",
                        "+48 699 300 299",
                        savedAddress
                )
        );

        assertTrue(customerRepository.existsByEmail("adrian@nowak.com"));
        assertFalse(customerRepository.existsByEmail("  ADRIAN@NOWAK.COM  "));
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        Address address = createValidAddress();
        Address savedAddress = addressRepository.save(address);

        customerRepository.save(
                new Customer(
                        "Adrian",
                        "Nowak",
                        "adrian@nowak.com",
                        "+48 699 300 299",
                        savedAddress
                )
        );

        assertFalse(customerRepository.existsByEmail("unknown@mail.com"));
    }

    @Test
    void shouldDeleteExistingCustomer() {
        Address address = createValidAddress();
        Address savedAddress = addressRepository.save(address);

        Customer savedCustomer = customerRepository.save(
                new Customer(
                        "Adrian",
                        "Nowak",
                        "adrian@nowak.com",
                        "+48 699 300 299",
                        savedAddress
                )
        );

        customerRepository.deleteById(savedCustomer.getId());

        Optional<Customer> deletedCustomer = customerRepository.findById(savedCustomer.getId());

        assertTrue(deletedCustomer.isEmpty());
    }


    @Test
    void shouldReturnIndependentListFromFindAll() {
        Address address = createValidAddress();
        Address savedAddress = addressRepository.save(address);

        customerRepository.save(
                new Customer(
                        "Adrian",
                        "Nowak",
                        "adrian@nowak.com",
                        "+48 699 300 299",
                        savedAddress
                )
        );

        List<Customer> foundCustomers = customerRepository.findAll();
        foundCustomers.clear();

        assertEquals(1, customerRepository.findAll().size());
    }

    @Test
    void shouldGenerateUniqueCustomerIds() {
        Address address = createValidAddress();
        Address savedAddress = addressRepository.save(address);

        Customer savedCustomer1 = customerRepository.save(
                new Customer(
                        "Adrian",
                        "Nowak",
                        "adrian@nowak.com",
                        "+48 699 300 299",
                        savedAddress
                )
        );

        Customer savedCustomer2 = customerRepository.save(
                new Customer(
                        "Franciszek",
                        "Cyprian",
                        "franciszek@cyprian.com",
                        "+48 777 222 333",
                        savedAddress
                )
        );

        assertNotNull(savedCustomer1.getId());
        assertNotNull(savedCustomer2.getId());
        assertNotEquals(savedCustomer1.getId(), savedCustomer2.getId());
    }

    private Address createValidAddress() {
        return new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
    }
}
