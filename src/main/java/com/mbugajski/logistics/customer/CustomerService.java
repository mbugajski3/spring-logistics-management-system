package com.mbugajski.logistics.customer;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer create(CreateCustomerRequest customerRequest) {
        if (customerRequest == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }

        String email = customerRequest.getEmail();

        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Customer with this email already exists.");
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

        return customerRepository.create(firstName, lastName, email, phoneNumber, createdAddress);
    }

    public Customer findById(long customerId) {
        Optional<Customer> foundCustomer = customerRepository.findById(customerId);

        if (foundCustomer.isEmpty()) {
            throw new CustomerNotFoundException(customerId);
        }

        return foundCustomer.get();
    }

    public void deleteById(long customerId) {
        findById(customerId);
        customerRepository.deleteById(customerId);
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }
}
