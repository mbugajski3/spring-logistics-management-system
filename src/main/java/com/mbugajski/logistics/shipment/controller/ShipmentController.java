package com.mbugajski.logistics.shipment.controller;

import com.mbugajski.logistics.shipment.dto.request.CreateShipmentRequest;
import com.mbugajski.logistics.shipment.dto.response.ShipmentResponse;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.mapper.ShipmentMapper;
import com.mbugajski.logistics.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public List<ShipmentResponse> findAll() {
        return shipmentService.findAll()
                .stream()
                .map(ShipmentMapper::toResponse)
                .toList();
    }

    @GetMapping("/{shipmentId}")
    public ShipmentResponse findById(@PathVariable Long shipmentId) {
        Shipment shipment = shipmentService.findById(shipmentId);

        return ShipmentMapper.toResponse(shipment);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponse create(@Valid @RequestBody CreateShipmentRequest shipmentRequest) {
        Shipment shipment = shipmentService.create(shipmentRequest);

        return ShipmentMapper.toResponse(shipment);
    }

    @PatchMapping("/{shipmentId}/ready-for-pickup")
    public ShipmentResponse markAsReadyForPickup(@PathVariable Long shipmentId) {
        Shipment shipment = shipmentService.markAsReadyForPickup(shipmentId);

        return ShipmentMapper.toResponse(shipment);
    }

    @PatchMapping("/{shipmentId}/in-transit")
    public ShipmentResponse markAsInTransit(@PathVariable Long shipmentId) {
        Shipment shipment = shipmentService.markAsInTransit(shipmentId);

        return ShipmentMapper.toResponse(shipment);
    }

    @PatchMapping("/{shipmentId}/delivered")
    public ShipmentResponse markAsDelivered(@PathVariable Long shipmentId) {
        Shipment shipment = shipmentService.markAsDelivered(shipmentId);

        return ShipmentMapper.toResponse(shipment);
    }

    @PatchMapping("/{shipmentId}/cancelled")
    public ShipmentResponse markAsCancelled(@PathVariable Long shipmentId) {
        Shipment shipment = shipmentService.markAsCancelled(shipmentId);

        return ShipmentMapper.toResponse(shipment);
    }
}
