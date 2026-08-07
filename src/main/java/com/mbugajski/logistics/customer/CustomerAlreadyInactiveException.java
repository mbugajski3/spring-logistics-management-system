package com.mbugajski.logistics.customer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CustomerAlreadyInactiveException extends RuntimeException {
    public CustomerAlreadyInactiveException() {
        super("Customer is already inactive.");
    }
}
