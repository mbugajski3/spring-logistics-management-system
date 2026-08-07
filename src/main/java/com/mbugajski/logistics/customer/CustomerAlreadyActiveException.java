package com.mbugajski.logistics.customer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CustomerAlreadyActiveException extends RuntimeException {
    public CustomerAlreadyActiveException() {
        super("Cannot activate already active customer.");
    }
}
