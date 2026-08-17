package com.mbugajski.logistics.vehicle.exception;

public class VehicleRegistrationNumberAlreadyExistsException extends RuntimeException {
    public VehicleRegistrationNumberAlreadyExistsException(String registrationNumber) {
        super(registrationNumber + " is already assigned to another vehicle.");
    }
}
