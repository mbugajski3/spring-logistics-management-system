package com.mbugajski.logistics.shipment.exception;

public class ShipmentInvalidWeightException extends RuntimeException {
    public ShipmentInvalidWeightException() {
        super("A shipment weight cannot be zero or negative");
    }
}
