package com.mbugajski.logistics.assignment.dto.response;

import com.mbugajski.logistics.assignment.entity.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ShipmentAssignmentResponse {

    private Long assignmentId;
    private Long shipmentId;
    private Long courierId;
    private Long vehicleId;
    private AssignmentStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime finishedAt;
}
