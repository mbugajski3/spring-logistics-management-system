package com.mbugajski.logistics.assignment.controller;

import com.mbugajski.logistics.assignment.dto.request.CreateShipmentAssignmentRequest;
import com.mbugajski.logistics.assignment.dto.response.ShipmentAssignmentResponse;
import com.mbugajski.logistics.assignment.entity.ShipmentAssignment;
import com.mbugajski.logistics.assignment.mapper.ShipmentAssignmentMapper;
import com.mbugajski.logistics.assignment.service.ShipmentAssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentAssignmentController {

    private final ShipmentAssignmentService shipmentAssignmentService;

    public ShipmentAssignmentController(ShipmentAssignmentService shipmentAssignmentService) {
        this.shipmentAssignmentService = shipmentAssignmentService;
    }

    @PostMapping("/{shipmentId}/assignment")
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentAssignmentResponse assign(@PathVariable Long shipmentId, @RequestBody @Valid CreateShipmentAssignmentRequest request) {
        ShipmentAssignment shipmentAssignment = shipmentAssignmentService.assign(shipmentId, request.getCourierId(), request.getVehicleId());

        return ShipmentAssignmentMapper.toResponse(shipmentAssignment);
    }

    @PatchMapping("/{shipmentId}/reassign")
    public ShipmentAssignmentResponse reassign(@PathVariable Long shipmentId) {
        ShipmentAssignment shipmentAssignment = shipmentAssignmentService.reassign(shipmentId);

        return ShipmentAssignmentMapper.toResponse(shipmentAssignment);
    }
}
