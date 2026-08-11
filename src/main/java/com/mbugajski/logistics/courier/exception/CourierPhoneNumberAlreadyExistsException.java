package com.mbugajski.logistics.courier.exception;

public class CourierPhoneNumberAlreadyExistsException extends RuntimeException {
    public CourierPhoneNumberAlreadyExistsException() {
        super("Courier with this phone number already exists.");
    }
}
