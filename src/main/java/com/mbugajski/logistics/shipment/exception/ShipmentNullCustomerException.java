package com.mbugajski.logistics.shipment.exception;

public class ShipmentNullCustomerException extends RuntimeException {
    public ShipmentNullCustomerException() {
        super("Customer cannot be null.");
    }
}
