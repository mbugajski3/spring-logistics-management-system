package com.mbugajski.logistics.shipment.exception;

public class ShipmentInvalidStatusException extends RuntimeException {
    public ShipmentInvalidStatusException(String message) {
        super(message);
    }
}
