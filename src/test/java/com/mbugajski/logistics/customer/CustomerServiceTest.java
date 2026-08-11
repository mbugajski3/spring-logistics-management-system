package com.mbugajski.logistics.customer;

import com.mbugajski.logistics.address.dto.request.CreateAddressRequest;
import com.mbugajski.logistics.customer.dto.request.CreateCustomerRequest;
import com.mbugajski.logistics.customer.dto.request.UpdateCustomerRequest;
import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.customer.exception.*;
import com.mbugajski.logistics.address.repository.AddressRepository;
import com.mbugajski.logistics.customer.repository.CustomerRepository;
import com.mbugajski.logistics.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private CustomerService testService;

    @Test
    void shouldCreateCustomer() {
        CreateAddressRequest addressRequest = createValidAddressRequest();
        CreateCustomerRequest customerRequest =
                createCustomerRequest(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 699 300 299");
        customerRequest.setAddress(addressRequest);

        when(customerRepository.existsByEmail(customerRequest.getEmail())).thenReturn(false);
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customer createdCustomer = testService.create(customerRequest);

        assertEquals(customerRequest.getFirstName(), createdCustomer.getFirstName());
        assertEquals(customerRequest.getLastName(), createdCustomer.getLastName());
        assertEquals(customerRequest.getEmail(), createdCustomer.getEmail());
        assertEquals(customerRequest.getPhoneNumber(), createdCustomer.getPhoneNumber());

        assertEquals(addressRequest.getStreet(), createdCustomer.getAddress().getStreet());
        assertEquals(addressRequest.getCity(), createdCustomer.getAddress().getCity());

        verify(addressRepository).save(any(Address.class));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void shouldRejectCustomerWithExistingEmail() {
        CreateAddressRequest addressRequest = createValidAddressRequest();

        CreateCustomerRequest customerRequest1 = createCustomerRequest(
                "Franciszek",
                "Cyprian",
                "  ADRIAN@NOWAK.COM  ",
                "+48 777 222 333");

        customerRequest1.setAddress(addressRequest);

        when(customerRepository.existsByEmail("adrian@nowak.com")).thenReturn(true);

        CustomerEmailAlreadyExistsException exception = assertThrows(CustomerEmailAlreadyExistsException.class, () -> testService.create(customerRequest1));
        assertEquals("Customer with email 'adrian@nowak.com' already exists.", exception.getMessage());

        verify(customerRepository, never()).save(any(Customer.class));
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void shouldRejectNullCustomer() {
        assertThrows(IllegalArgumentException.class, () -> testService.create(null));
    }

    @Test
    void shouldFindCustomerById() {
        Address address = createAddress();

        Customer customer =
                new Customer(
                        "Adrian",
                        "Nowak",
                        "adrian@nowak.com",
                        "+48 699 300 299",
                        address
                );

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Customer foundCustomer = testService.findById(1L);

        assertSame(customer, foundCustomer);

        verify(customerRepository).findById(1L);
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
        Address savedAddress = createAddress();
        Customer customer1 = new Customer(
                        "Adrian",
                        "Nowak",
                        "adrian@nowak.com",
                        "+48 699 300 299",
                        savedAddress
        );

        Customer customer2 = new Customer(
                        "Franciszek",
                        "Cyprian",
                        "franciszek@cyprian.com",
                        "+48 777 222 333",
                        savedAddress
        );

        List<Customer> customers = List.of(customer1, customer2);

        when(customerRepository.findAll()).thenReturn(customers);

        List<Customer> foundCustomers = testService.findAll();

        assertEquals(2, foundCustomers.size());
        assertTrue(foundCustomers.contains(customer1));
        assertTrue(foundCustomers.contains(customer2));

        verify(customerRepository).findAll();
    }

    @Test
    void shouldUpdateOnlyProvidedCustomerFields() {
        Address address = createAddress();

        Customer customer =
                new Customer(
                        "Adrian",
                        "Nowak",
                        "adrian@nowak.com",
                        "+48 699 300 299",
                        address
                );

        UpdateCustomerRequest updateCustomerRequest = new UpdateCustomerRequest();
        updateCustomerRequest.setLastName("Kowalski");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer updatedCustomer = testService.update(1L, updateCustomerRequest);

        assertEquals("Kowalski", updatedCustomer.getLastName());
        assertEquals("Adrian", updatedCustomer.getFirstName());
        assertEquals("adrian@nowak.com", updatedCustomer.getEmail());
        assertEquals("+48 699 300 299", updatedCustomer.getPhoneNumber());
        assertEquals(address, updatedCustomer.getAddress());

        verify(customerRepository).save(customer);
    }

    @Test
    void shouldRejectUpdateWithNoProvidedFields() {
        Address address = createAddress();

        Customer customer =
                new Customer(
                        "Adrian",
                        "Nowak",
                        "adrian@nowak.com",
                        "+48 699 300 299",
                        address
                );

        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        EmptyCustomerUpdateException exception = assertThrows(EmptyCustomerUpdateException.class, () -> testService.update(1L, updateRequest));

        assertEquals("At least one field must be provided.", exception.getMessage());
        assertEquals("Adrian", customer.getFirstName());
        assertEquals("Nowak", customer.getLastName());
    }

    @Test
    void shouldRejectNullUpdateRequest() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> testService.update(1L, null));

        assertEquals("Customer request cannot be null.", exception.getMessage());

        verifyNoInteractions(customerRepository);
    }

    @Test
    void shouldRejectUpdateForNonExistingCustomer() {
        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest();
        updateRequest.setLastName("Kowalski");

        when(customerRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> testService.update(999L, updateRequest));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldUpdateMultipleProvidedFields() {
        Address address = createAddress();
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 699 300 299",
                address);

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

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer updatedCustomer = testService.update(1L, updateCustomerRequest);
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
        assertNotSame(address, updatedAddress);

        verify(addressRepository).save(any(Address.class));
        verify(customerRepository).save(customer);
    }

    @Test
    void shouldUpdateEmailWhenAvailable() {
        Address address = createAddress();
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 699 300 299",
                address);

        UpdateCustomerRequest customerRequest = new UpdateCustomerRequest();
        customerRequest.setEmail("NEW.EMAIL@EXAMPLE.COM  ");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail("new.email@example.com")).thenReturn(false);
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer updatedCustomer = testService.update(1L, customerRequest);

        assertEquals("Adrian", updatedCustomer.getFirstName());
        assertEquals("Nowak", updatedCustomer.getLastName());
        assertEquals("new.email@example.com", updatedCustomer.getEmail());
        assertEquals("+48 699 300 299", updatedCustomer.getPhoneNumber());
        assertEquals(address, updatedCustomer.getAddress());

        verify(customerRepository).existsByEmail("new.email@example.com");
    }

    @Test
    void shouldAllowUpdatingCustomerWithOwnEmail() {
        Address address = createAddress();
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 699 300 299",
                address);

        UpdateCustomerRequest customerRequest = new UpdateCustomerRequest();
        customerRequest.setEmail("  ADRIAN@NOWAK.COM  ");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer updatedCustomer = testService.update(1L, customerRequest);

        assertEquals("Adrian", updatedCustomer.getFirstName());
        assertEquals("Nowak", updatedCustomer.getLastName());
        assertEquals("adrian@nowak.com", updatedCustomer.getEmail());
        assertEquals("+48 699 300 299", updatedCustomer.getPhoneNumber());
        assertEquals(address, updatedCustomer.getAddress());

        verify(customerRepository, never()).existsByEmail(anyString());
    }

    @Test
    void shouldRejectEmailUsedByAnotherCustomer() {
        Address address = createAddress();
        Customer customer1 = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 699 300 299",
                address
        );

        UpdateCustomerRequest customerRequest = new UpdateCustomerRequest();
        customerRequest.setEmail("piotr@kowalski.com");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer1));
        when(customerRepository.existsByEmail(customerRequest.getEmail())).thenReturn(true);

        CustomerEmailAlreadyExistsException exception = assertThrows(CustomerEmailAlreadyExistsException.class, () -> testService.update(1L, customerRequest));

        assertEquals("Customer with email 'piotr@kowalski.com' already exists.", exception.getMessage());
        assertEquals("Adrian", customer1.getFirstName());
        assertEquals("Nowak", customer1.getLastName());
        assertEquals("adrian@nowak.com", customer1.getEmail());
        assertEquals("+48 699 300 299", customer1.getPhoneNumber());
        assertEquals(address, customer1.getAddress());

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldActivateCustomer() {
        Address address = createAddress();
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 699 300 299",
                address
        );
        customer.deactivate();
        assertFalse(customer.isActive());

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer activatedCustomer = testService.activate(1L);

        assertTrue(activatedCustomer.isActive());
        assertSame(customer, activatedCustomer);

        verify(customerRepository).save(customer);
    }

    @Test
    void shouldRejectActivationOfActiveCustomer() {
        Address address = createAddress();
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 699 300 299",
                address
        );

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerAlreadyActiveException exception = assertThrows(CustomerAlreadyActiveException.class, () -> testService.activate(1L));

        assertEquals("Cannot activate already active customer.", exception.getMessage());
        assertTrue(customer.isActive());

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldDeactivateActiveCustomerWithoutDebt() {
        Address address = createAddress();
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 699 300 299",
                address
        );

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer inactiveCustomer = testService.deactivate(1L);

        assertFalse(inactiveCustomer.isActive());
        assertEquals(new BigDecimal("0.00"), inactiveCustomer.getDebt());

        verify(customerRepository).save(customer);
    }

    @Test
    void shouldRejectDeactivateInactiveCustomer() {
        Address address = createAddress();
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 699 300 299",
                address
        );
        customer.deactivate();
        assertFalse(customer.isActive());

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerAlreadyInactiveException exception = assertThrows(CustomerAlreadyInactiveException.class, () -> testService.deactivate(1L));

        assertEquals("Customer is already inactive.", exception.getMessage());
        assertFalse(customer.isActive());

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldRejectDeactivationOfCustomerWithDebt() {
        Address address = createAddress();
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 699 300 299",
                address
        );
        customer.addDebt(new BigDecimal("200.00"));
        assertEquals(new BigDecimal("200.00"), customer.getDebt());

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerHasOutstandingDebtException exception = assertThrows(CustomerHasOutstandingDebtException.class, () -> testService.deactivate(1L));

        assertEquals("Customer with debt cannot be deactivated.", exception.getMessage());
        assertTrue(customer.isActive());
        assertEquals(new BigDecimal("200.00"), customer.getDebt());

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldRejectActivationOfNonExistingCustomer() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () -> testService.activate(999L));

        assertEquals("Customer with ID 999 was not found.", exception.getMessage());

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldRejectDeletionOfActiveCustomer() {
        Address address = createAddress();
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 699 300 299",
                address
        );

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        ActiveCustomerDeletionException exception = assertThrows(ActiveCustomerDeletionException.class, () -> testService.deleteById(1L));

        assertEquals("Cannot delete active customer.", exception.getMessage());

        verify(customerRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldDeleteInactiveCustomer() {
        Address address = createAddress();
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 699 300 299",
                address
        );
        customer.deactivate();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        testService.deleteById(1L);

        verify(customerRepository).deleteById(1L);
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
        return new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
    }
}
