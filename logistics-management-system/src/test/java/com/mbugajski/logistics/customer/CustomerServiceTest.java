package com.mbugajski.logistics.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CustomerServiceTest {

    private CustomerRepository testRepository;
    private CustomerService testService;

    @BeforeEach
    void setUp() {
        testRepository = new CustomerRepository();
        testService = new CustomerService(testRepository);
    }

    @Test
    void shouldCreateCustomer() {
        CreateAddressRequest addressRequest = createValidAddressRequest();
        CreateCustomerRequest customerRequest = createCustomerRequest("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299");
        customerRequest.setAddress(addressRequest);

        Customer createdCustomer = testService.create(customerRequest);
        List<Customer> foundCustomers = testRepository.findAll();

        assertEquals(1L, createdCustomer.getId());
        assertEquals(customerRequest.getFirstName(), createdCustomer.getFirstName());
        assertEquals(addressRequest.getStreet(), createdCustomer.getAddress().getStreet());
        assertTrue(foundCustomers.contains(createdCustomer));
    }

    @Test
    void shouldRejectCustomerWithExistingEmail() {
        CreateAddressRequest addressRequest = createValidAddressRequest();
        CreateCustomerRequest customerRequest1 = createCustomerRequest("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299");
        CreateCustomerRequest customerRequest2 = createCustomerRequest("Franciszek", "Cyprian", "  ADRIAN@NOWAK.COM  ", "+48 777 222 333");

        customerRequest1.setAddress(addressRequest);
        customerRequest2.setAddress(addressRequest);

        testService.create(customerRequest1);

        assertThrows(IllegalArgumentException.class, () -> testService.create(customerRequest2));
        assertEquals(1, testRepository.findAll().size());
    }

    @Test
    void shouldRejectNullCustomer() {
        assertThrows(IllegalArgumentException.class, () -> testService.create(null));
    }

    @Test
    void shouldFindCustomerById() {
        CreateAddressRequest addressRequest = createValidAddressRequest();
        CreateCustomerRequest customerRequest = createCustomerRequest("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299");
        customerRequest.setAddress(addressRequest);

        Customer createdCustomer = testService.create(customerRequest);
        Customer foundCustomer = testService.findById(createdCustomer.getId());

        assertSame(createdCustomer, foundCustomer);
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () -> testService.findById(1L));
        assertEquals("Customer with ID 1 was not found.", exception.getMessage());
    }

    @Test
    void shouldDeleteCustomerById() {
        CreateAddressRequest addressRequest = createValidAddressRequest();
        CreateCustomerRequest customerRequest = createCustomerRequest("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299");
        customerRequest.setAddress(addressRequest);

        Customer createdCustomer = testService.create(customerRequest);
        testService.deleteById(createdCustomer.getId());

        assertTrue(testRepository.findById(createdCustomer.getId()).isEmpty());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenDeletingNonExistingCustomer() {
        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () -> testService.deleteById(1L));

        assertEquals("Customer with ID 1 was not found.", exception.getMessage());
    }

    @Test
    void shouldReturnAllCustomers() {
        CreateAddressRequest addressRequest = createValidAddressRequest();
        CreateCustomerRequest customerRequest1 = createCustomerRequest("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299");
        CreateCustomerRequest customerRequest2 = createCustomerRequest("Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333");

        customerRequest1.setAddress(addressRequest);
        customerRequest2.setAddress(addressRequest);

        Customer customer1 = testService.create(customerRequest1);
        Customer customer2 = testService.create(customerRequest2);
        List<Customer> foundCustomers = testService.findAll();

        assertEquals(2, foundCustomers.size());
        assertTrue(foundCustomers.contains(customer1));
        assertTrue(foundCustomers.contains(customer2));
    }

    private CreateCustomerRequest createCustomerRequest(String firstName, String lastName, String email, String phoneNumber) {
        CreateCustomerRequest customerRequest = new CreateCustomerRequest();

        customerRequest.setFirstName(firstName);
        customerRequest.setLastName(lastName);
        customerRequest.setEmail(email);
        customerRequest.setPhoneNumber(phoneNumber);

        return customerRequest;
    }

    private CreateAddressRequest createValidAddressRequest() {
        CreateAddressRequest addressRequest = new CreateAddressRequest();
        addressRequest.setStreet("Wschodnia");
        addressRequest.setBuildingNumber("130");
        addressRequest.setApartmentNumber("15");
        addressRequest.setCity("Łódź");
        addressRequest.setPostalCode("90-266");
        addressRequest.setCountry("Poland");

        return addressRequest;
    }
}
