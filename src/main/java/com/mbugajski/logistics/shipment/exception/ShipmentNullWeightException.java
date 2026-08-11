package com.mbugajski.logistics.shipment.exception;

public class ShipmentNullWeightException extends RuntimeException {
    public ShipmentNullWeightException() {
        super("Shipment weight cannot be null.");
    }
}
