package com.mbugajski.logistics.customer;

import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer create(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }

        String customerEmail = customer.getEmail();

        if (customerRepository.existsByEmail(customerEmail)) {
            throw new IllegalArgumentException("Customer with this email already exists.");
        }

        return customerRepository.create(customer);
    }
}
