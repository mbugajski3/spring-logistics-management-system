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
