package com.mbugajski.logistics.customer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(long customerId) {
        super("Customer with ID " + customerId + " was not found.");
    }
}
