package com.mbugajski.logistics.vehicle.exception;

import java.math.BigDecimal;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(Long vehicleId) {
        super("Vehicle with id " + vehicleId + " not found.");
    }

    public VehicleNotFoundException(BigDecimal shipmentWeight) {
        super("No suitable vehicle available for shipment weighing " + shipmentWeight + " kg.");
    }



}
