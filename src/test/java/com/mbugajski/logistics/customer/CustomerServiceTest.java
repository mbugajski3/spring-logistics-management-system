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

        CustomerEmailAlreadyExistsException exception = assertThrows(CustomerEmailAlreadyExistsException.class, () -> testService.create(customerRequest2));
        assertEquals("Customer with email 'adrian@nowak.com' already exists.", exception.getMessage());
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

    @Test
    void shouldUpdateOnlyProvidedCustomerFields() {
        Address address = createAddress();
        Customer customer = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        UpdateCustomerRequest updateCustomerRequest = new UpdateCustomerRequest();

        updateCustomerRequest.setLastName("Kowalski");

        Customer updatedCustomer = testService.update(customer.getId(), updateCustomerRequest);

        assertEquals("Kowalski", updatedCustomer.getLastName());
        assertEquals("Adrian", updatedCustomer.getFirstName());
        assertEquals("adrian@nowak.com", updatedCustomer.getEmail());
        assertEquals("+48 699 300 299", updatedCustomer.getPhoneNumber());
        assertEquals(address, updatedCustomer.getAddress());
    }

    @Test
    void shouldRejectUpdateWithNoProvidedFields() {
        Address address = createAddress();
        Customer customer = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest();

        EmptyCustomerUpdateException exception = assertThrows(EmptyCustomerUpdateException.class, () -> testService.update(customer.getId(), updateRequest));

        assertEquals("At least one field must be provided.", exception.getMessage());
        assertEquals("Adrian", customer.getFirstName());
        assertEquals("Nowak", customer.getLastName());
    }

    @Test
    void shouldRejectNullUpdateRequest() {
        Address address = createAddress();
        Customer customer = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> testService.update(customer.getId(), null));

        assertEquals("Customer request cannot be null.", exception.getMessage());

        assertEquals("Nowak", customer.getLastName());
        assertEquals("Adrian", customer.getFirstName());
        assertEquals("adrian@nowak.com", customer.getEmail());
        assertEquals("+48 699 300 299", customer.getPhoneNumber());
        assertEquals(address, customer.getAddress());
    }

    @Test
    void shouldRejectUpdateForNonExistingCustomer() {
        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest();
        updateRequest.setLastName("Kowalski");

        assertThrows(CustomerNotFoundException.class,() -> testService.update(999L, updateRequest));
    }

    @Test
    void shouldUpdateMultipleProvidedFields() {
        Address oldAddress = createAddress();
        Customer customer = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", oldAddress);

        UpdateCustomerRequest updateCustomerRequest = new UpdateCustomerRequest();
        CreateAddressRequest addressRequest = new CreateAddressRequest();

        addressRequest.setStreet("Zachodnia");
        addressRequest.setBuildingNumber("330");
        addressRequest.setApartmentNumber("20");
        addressRequest.setCity("Kielce");
        addressRequest.setPostalCode("70-230");
        addressRequest.setCountry("Poland");

        updateCustomerRequest.setFirstName("Piotr");
        updateCustomerRequest.setPhoneNumber("+48 585 233 132");
        updateCustomerRequest.setAddress(addressRequest);

        Customer updatedCustomer = testService.update(customer.getId(), updateCustomerRequest);
        Address updatedAddress = updatedCustomer.getAddress();

        assertEquals("Piotr", updatedCustomer.getFirstName());
        assertEquals("Nowak", updatedCustomer.getLastName());
        assertEquals("adrian@nowak.com", updatedCustomer.getEmail());
        assertEquals("+48 585 233 132", updatedCustomer.getPhoneNumber());
        assertEquals("Zachodnia", updatedAddress.getStreet());
        assertEquals("330", updatedAddress.getBuildingNumber());
        assertEquals("20", updatedAddress.getApartmentNumber());
        assertEquals("Kielce", updatedAddress.getCity());
        assertEquals("70-230", updatedAddress.getPostalCode());
        assertEquals("Poland", updatedAddress.getCountry());
        assertNotSame(oldAddress, updatedAddress);
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

    private Address createAddress() {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");

        return address;
    }

    private Address createNewAddress() {
        Address address = new Address("Zachodnia", "330", "20", "Kielce", "70-230", "Poland");

        return address;
    }
}
