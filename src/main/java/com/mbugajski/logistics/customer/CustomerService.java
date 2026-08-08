package com.mbugajski.logistics.customer;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    public CustomerService(CustomerRepository customerRepository, AddressRepository addressRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public Customer create(CreateCustomerRequest customerRequest) {
        if (customerRequest == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }

        String email = customerRequest.getEmail().trim().toLowerCase();

        if (customerRepository.existsByEmail(email)) {
            throw new CustomerEmailAlreadyExistsException(email);
        }

        String firstName = customerRequest.getFirstName();
        String lastName = customerRequest.getLastName();
        String phoneNumber = customerRequest.getPhoneNumber();

        CreateAddressRequest addressRequest = customerRequest.getAddress();
        String street = addressRequest.getStreet();
        String buildingNumber = addressRequest.getBuildingNumber();
        String apartmentNumber = addressRequest.getApartmentNumber();
        String city = addressRequest.getCity();
        String postalCode = addressRequest.getPostalCode();
        String country = addressRequest.getCountry();

        Address createdAddress = new Address(street, buildingNumber, apartmentNumber, city, postalCode, country);
        addressRepository.save(createdAddress);

        Customer createdCustomer = new Customer(firstName, lastName, email, phoneNumber, createdAddress);

        return customerRepository.save(createdCustomer);
    }

    public Customer findById(long customerId) {
        Optional<Customer> foundCustomer = customerRepository.findById(customerId);

        if (foundCustomer.isEmpty()) {
            throw new CustomerNotFoundException(customerId);
        }

        return foundCustomer.get();
    }

    public void deleteById(long customerId) {
        Customer customerFound = findById(customerId);

        if (customerFound.isActive()) {
            throw new ActiveCustomerDeletionException();
        }

        customerRepository.deleteById(customerId);
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Transactional
    public Customer update(long customerId, UpdateCustomerRequest customerRequest) {
        if (customerRequest == null) {
            throw new IllegalArgumentException("Customer request cannot be null.");
        }

        Optional<Customer> foundCustomer = customerRepository.findById(customerId);

        if (foundCustomer.isEmpty()) {
            throw new CustomerNotFoundException(customerId);
        }

        Customer customer = foundCustomer.get();

        if (customerRequest.hasNoUpdates()) {
            throw new EmptyCustomerUpdateException();
        }

        if (customerRequest.getEmail() != null) {
            String requestedEmail = customerRequest.getEmail().trim().toLowerCase();

            boolean emailChanged = !customer.getEmail().equalsIgnoreCase(requestedEmail);

            if (emailChanged && customerRepository.existsByEmail(requestedEmail)) {
                throw new CustomerEmailAlreadyExistsException(requestedEmail);
            }

            if (emailChanged) {
                customer.changeEmail(requestedEmail);
            }
        }

        if (customerRequest.getFirstName() != null) {
            customer.changeFirstName(customerRequest.getFirstName());
        }

        if (customerRequest.getLastName() != null) {
            customer.changeLastName(customerRequest.getLastName());
        }

        if (customerRequest.getPhoneNumber() != null) {
            customer.changePhoneNumber(customerRequest.getPhoneNumber());
        }

        if (customerRequest.getAddress() != null) {
            CreateAddressRequest addressRequest = customerRequest.getAddress();

            Address address = new Address(
                    addressRequest.getStreet(),
                    addressRequest.getBuildingNumber(),
                    addressRequest.getApartmentNumber(),
                    addressRequest.getCity(),
                    addressRequest.getPostalCode(),
                    addressRequest.getCountry()
            );

            Address savedAddress = addressRepository.save(address);

            customer.changeAddress(savedAddress);
        }

        return customerRepository.save(customer);
    }

    public Customer activate(long customerId) {
        Customer customer = findById(customerId);
        customer.activate();

        return customerRepository.save(customer);
    }

    public Customer deactivate(long customerId) {
        Customer customer = findById(customerId);
        customer.deactivate();

        return customerRepository.save(customer);
    }
}
