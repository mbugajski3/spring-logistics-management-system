package com.mbugajski.logistics.customer;

import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class CustomerRepository {

    private final Map<Long, Customer> customers = new HashMap<>();

    public Customer create(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }

        if (customers.containsKey(customer.getId())) {
            throw new IllegalArgumentException("Customer with this ID already exists.");
        }

        customers.put(customer.getId(), customer);

        return customer;
    }

    public Optional<Customer> findById(long customerId) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Customer ID cannot be 0 or below.");
        }

        return Optional.ofNullable(customers.get(customerId));
    }

    public List<Customer> findAll() {
        return new ArrayList<>(customers.values());
    }

    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty.");
        }

        String cleanEmail = email.trim();

        for (Customer customer : customers.values()) {
            if (cleanEmail.equalsIgnoreCase(customer.getEmail())) {
                return true;
            }
        }

        return false;
    }

    public boolean deleteById(long customerId) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Customer ID cannot be 0 or below.");
        }

        if (customers.containsKey(customerId)) {
            customers.remove(customerId);

            return true;
        }

        return false;
    }
}
