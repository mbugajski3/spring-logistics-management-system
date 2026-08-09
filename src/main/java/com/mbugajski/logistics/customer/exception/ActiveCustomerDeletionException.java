package com.mbugajski.logistics.customer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ActiveCustomerDeletionException extends RuntimeException {
    public ActiveCustomerDeletionException() {
        super("Cannot delete active customer.");
    }
}
