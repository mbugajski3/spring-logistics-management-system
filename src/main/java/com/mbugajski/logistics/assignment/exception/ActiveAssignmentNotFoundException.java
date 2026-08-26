package com.mbugajski.logistics.assignment.exception;

public class ActiveAssignmentNotFoundException extends RuntimeException {
    public ActiveAssignmentNotFoundException(Long shipmentId) {
        super("Active assignment for shipment with id " + shipmentId + " not found.");
    }
}
