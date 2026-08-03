package com.mbugajski.logistics.customer;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(long customerId) {
        super("Customer with ID " + customerId + " was not found.");
    }
}
