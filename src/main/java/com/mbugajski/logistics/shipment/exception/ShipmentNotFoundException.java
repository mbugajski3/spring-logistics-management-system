package com.mbugajski.logistics.shipment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ShipmentNotFoundException extends RuntimeException {
    public ShipmentNotFoundException(Long shipmentId) {
        super("Shipment with id " + shipmentId + " not found.");
    }
}
