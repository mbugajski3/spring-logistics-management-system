package com.mbugajski.logistics.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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

    @Test
    void shouldUpdateEmailWhenAvailable() {
        Address address = createAddress();
        Customer customer = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        UpdateCustomerRequest customerRequest = new UpdateCustomerRequest();
        customerRequest.setEmail("NEW.EMAIL@EXAMPLE.COM  ");

        Customer updatedCustomer = testService.update(customer.getId(), customerRequest);

        assertEquals("Adrian", updatedCustomer.getFirstName());
        assertEquals("Nowak", updatedCustomer.getLastName());
        assertEquals("new.email@example.com", updatedCustomer.getEmail());
        assertEquals("+48 699 300 299", updatedCustomer.getPhoneNumber());
        assertEquals(address, updatedCustomer.getAddress());
    }

    @Test
    void shouldAllowUpdatingCustomerWithOwnEmail() {
        Address address = createAddress();
        Customer customer = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        UpdateCustomerRequest customerRequest = new UpdateCustomerRequest();
        customerRequest.setEmail("  ADRIAN@NOWAK.COM  ");

        Customer updatedCustomer = testService.update(customer.getId(), customerRequest);

        assertEquals("Adrian", updatedCustomer.getFirstName());
        assertEquals("Nowak", updatedCustomer.getLastName());
        assertEquals("adrian@nowak.com", updatedCustomer.getEmail());
        assertEquals("+48 699 300 299", updatedCustomer.getPhoneNumber());
        assertEquals(address, updatedCustomer.getAddress());
    }

    @Test
    void shouldRejectEmailUsedByAnotherCustomer() {
        Address address = createAddress();
        Customer customer1 = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);
        Customer customer2 = testRepository.create("Piotr", "Kowalski", "piotr@kowalski.com", "+48 700 300 110", address);

        UpdateCustomerRequest customerRequest = new UpdateCustomerRequest();
        customerRequest.setEmail("piotr@kowalski.com");

        CustomerEmailAlreadyExistsException exception = assertThrows(CustomerEmailAlreadyExistsException.class, () -> testService.update(customer1.getId(), customerRequest));

        assertEquals("Customer with email 'piotr@kowalski.com' already exists.", exception.getMessage());
        assertEquals("Adrian", customer1.getFirstName());
        assertEquals("Nowak", customer1.getLastName());
        assertEquals("adrian@nowak.com", customer1.getEmail());
        assertEquals("+48 699 300 299", customer1.getPhoneNumber());
        assertEquals(address, customer1.getAddress());

        assertEquals(2, testRepository.findAll().size());
    }

    @Test
    void shouldActivateCustomer() {
        Address address = createAddress();
        Customer customer1 = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);
        customer1.deactivate();
        assertFalse(customer1.isActive());

        Customer activatedCustomer = testService.activate(customer1.getId());

        assertTrue(activatedCustomer.isActive());
        assertSame(customer1, activatedCustomer);
    }

    @Test
    void shouldRejectActivationOfActiveCustomer() {
        Address address = createAddress();
        Customer customer = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        CustomerAlreadyActiveException exception = assertThrows(CustomerAlreadyActiveException.class, () -> testService.activate(customer.getId()));

        assertEquals("Cannot activate already active customer.", exception.getMessage());
        assertTrue(customer.isActive());
    }

    @Test
    void shouldDeactivateActiveCustomerWithoutDebt() {
        Address address = createAddress();
        Customer customer = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        Customer inactiveCustomer = testService.deactivate(customer.getId());

        assertFalse(inactiveCustomer.isActive());
        assertEquals(new BigDecimal("0.00"), inactiveCustomer.getDebt());
    }

    @Test
    void shouldRejectDeactivateInactiveCustomer() {
        Address address = createAddress();
        Customer customer = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);
        customer.deactivate();

        CustomerAlreadyInactiveException exception = assertThrows(CustomerAlreadyInactiveException.class, () -> testService.deactivate(customer.getId()));

        assertEquals("Customer is already inactive.", exception.getMessage());
        assertFalse(customer.isActive());
    }

    @Test
    void shouldRejectDeactivationOfCustomerWithDebt() {
        Address address = createAddress();
        Customer customer = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);
        customer.addDebt(new BigDecimal("200.00"));

        CustomerHasOutstandingDebtException exception = assertThrows(CustomerHasOutstandingDebtException.class, () -> testService.deactivate(customer.getId()));

        assertEquals("Customer with debt cannot be deactivated.", exception.getMessage());
        assertTrue(customer.isActive());
        assertEquals(new BigDecimal("200.00"), customer.getDebt());
    }

    @Test
    void shouldRejectActivationOfNonExisingCustomer() {
        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () -> testService.activate(999L));

        assertEquals("Customer with ID 999 was not found.", exception.getMessage());
    }

    @Test
    void shouldRejectDeletionOfActiveCustomer() {
        Address address = createAddress();
        Customer customer = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        ActiveCustomerDeletionException exception = assertThrows(ActiveCustomerDeletionException.class, () -> testService.deleteById(customer.getId()));

        Optional<Customer> foundCustomer = testRepository.findById(customer.getId());

        assertEquals("Cannot delete active customer.", exception.getMessage());
        assertTrue(foundCustomer.isPresent());
    }

    @Test
    void shouldDeleteInactiveCustomer() {
        Address address = createAddress();
        Customer customer = testRepository.create("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);
        customer.deactivate();

        testService.deleteById(customer.getId());

        Optional<Customer> foundCustomer = testRepository.findById(customer.getId());
        assertTrue(foundCustomer.isEmpty());
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
