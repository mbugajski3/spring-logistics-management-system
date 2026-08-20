package com.mbugajski.logistics.assignment.exception;

public class AssignmentNotFound extends RuntimeException {
    public AssignmentNotFound(Long assignmentId) {
        super("Assignment with id " + assignmentId + " not found.");
    }
}
