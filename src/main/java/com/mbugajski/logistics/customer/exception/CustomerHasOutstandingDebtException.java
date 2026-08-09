package com.mbugajski.logistics.customer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CustomerHasOutstandingDebtException extends RuntimeException {
    public CustomerHasOutstandingDebtException() {
        super("Customer with debt cannot be deactivated.");
    }
}
