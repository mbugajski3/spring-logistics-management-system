package com.mbugajski.logistics.customer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmptyCustomerUpdateException extends RuntimeException {

    public EmptyCustomerUpdateException() {
        super("At least one field must be provided.");
    }
}
