package com.mbugajski.logistics.vehicle.exception;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(Long vehicleId) {
        super("Vehicle with id " + vehicleId + " not found.");
    }
}
