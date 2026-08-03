package com.mbugajski.logistics.customer;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerServiceTest {

    @Test
    void shouldCreateCustomer() {
        CustomerRepository testRepository = new CustomerRepository();
        CustomerService testService = new CustomerService(testRepository);
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer1 = new Customer(1L, "Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        Customer createdCustomer = testService.create(customer1);
        List<Customer> foundCustomers = testRepository.findAll();

        assertSame(customer1, createdCustomer);
        assertTrue(foundCustomers.contains(createdCustomer));
    }

    @Test
    void shouldRejectCustomerWithExistingEmail() {
        CustomerRepository testRepository = new CustomerRepository();
        CustomerService testService = new CustomerService(testRepository);
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer1 = new Customer(1L, "Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);
        Customer customer2 = new Customer(2L, "Franciszek", "Cyprian", "ADRIAN@NOWAK.COM", "+48 777 222 333", address);

        testService.create(customer1);

        assertThrows(IllegalArgumentException.class, () -> testService.create(customer2));
        assertEquals(1, testRepository.findAll().size());
    }

    @Test
    void shouldRejectNullCustomer() {
        CustomerRepository testRepository = new CustomerRepository();
        CustomerService testService = new CustomerService(testRepository);

        assertThrows(IllegalArgumentException.class, () -> testService.create(null));
    }

    @Test
    void shouldFindCustomerById() {
        CustomerRepository testRepository = new CustomerRepository();
        CustomerService testService = new CustomerService(testRepository);
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer1 = new Customer(1L, "Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        Customer createdCustomer = testService.create(customer1);
        Customer foundCustomer = testService.findById(1L);

        assertSame(createdCustomer, foundCustomer);
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        CustomerRepository testRepository = new CustomerRepository();
        CustomerService testService = new CustomerService(testRepository);

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () -> testService.findById(1L));
        assertEquals("Customer with ID 1 was not found.", exception.getMessage());
    }

    @Test
    void shouldDeleteCustomerById() {
        CustomerRepository testRepository = new CustomerRepository();
        CustomerService testService = new CustomerService(testRepository);
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer1 = new Customer(1L, "Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        testService.create(customer1);
        testService.deleteById(1L);

        assertTrue(testRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenDeletingNonExistingCustomer() {
        CustomerRepository testRepository = new CustomerRepository();
        CustomerService testService = new CustomerService(testRepository);

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () -> testService.deleteById(1L));

        assertEquals("Customer with ID 1 was not found.", exception.getMessage());
    }

    @Test
    void shouldReturnAllCustomers() {
        CustomerRepository testRepository = new CustomerRepository();
        CustomerService testService = new CustomerService(testRepository);
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer1 = new Customer(1L, "Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);
        Customer customer2 = new Customer(2L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        testService.create(customer1);
        testService.create(customer2);
        List<Customer> foundCustomers = testService.findAll();

        assertEquals(2, foundCustomers.size());
        assertTrue(foundCustomers.contains(customer1));
        assertTrue(foundCustomers.contains(customer2));
    }
}
