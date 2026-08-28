package com.mbugajski.logistics.assignment.mapper;

import com.mbugajski.logistics.assignment.dto.response.ShipmentAssignmentResponse;
import com.mbugajski.logistics.assignment.entity.ShipmentAssignment;

public class ShipmentAssignmentMapper {
    public static ShipmentAssignmentResponse toResponse(ShipmentAssignment assignment) {
        return new ShipmentAssignmentResponse(
                assignment.getId(),
                assignment.getShipment().getId(),
                assignment.getCourier().getId(),
                assignment.getVehicle().getId(),
                assignment.getStatus(),
                assignment.getAssignedAt(),
                assignment.getFinishedAt()
        );
    }
}
