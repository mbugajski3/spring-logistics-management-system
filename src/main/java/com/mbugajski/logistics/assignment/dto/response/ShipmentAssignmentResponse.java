package com.mbugajski.logistics.assignment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShipmentAssignmentResponse {

    private Long assignmentId;
    private Long shipmentId;
    private Long courierId;
    private Long vehicleId;
}
