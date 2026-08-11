package com.mbugajski.logistics.shipment.exception;

import java.math.BigDecimal;

public class ShipmentOverweightException extends RuntimeException {
    public ShipmentOverweightException(BigDecimal weight) {
        super("Shipment cannot be created for weight " + weight.toString() + " kg.");
    }
}
