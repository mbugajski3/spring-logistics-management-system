package com.mbugajski.logistics.courier.exception;

import java.math.BigDecimal;

public class CourierNotFoundException extends RuntimeException {
    public CourierNotFoundException(Long courierId) {
        super("Courier with id " + courierId + " not found.");
    }

    public CourierNotFoundException() {
        super("Available courier not found.");
    }
}
