package com.mbugajski.logistics.shipment.controller;

import com.mbugajski.logistics.shipment.dto.request.CreateShipmentRequest;
import com.mbugajski.logistics.shipment.dto.response.ShipmentPaginationResponse;
import com.mbugajski.logistics.shipment.dto.response.ShipmentResponse;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.entity.ShipmentStatus;
import com.mbugajski.logistics.shipment.mapper.ShipmentMapper;
import com.mbugajski.logistics.shipment.repository.ShipmentSortBy;
import com.mbugajski.logistics.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public ShipmentPaginationResponse findAll(@RequestParam(name = "page", defaultValue = "0") int pageNumber,
                                              @RequestParam(name = "size", defaultValue = "20") int pageSize,
                                              @RequestParam(name = "status", required = false) ShipmentStatus status,
                                              @RequestParam(name = "customerId", required = false) Long customerId,
                                              @RequestParam(name = "sortBy", required = false) ShipmentSortBy sortParam,
                                              @RequestParam(name = "direction", required = false) String direction) {
        String normalizedDirection =
                direction == null ? null : direction.trim().toLowerCase();

        Page<Shipment> shipments = shipmentService.findAllByPageNumber(pageNumber, pageSize, status, customerId, sortParam, normalizedDirection);

        return ShipmentMapper.paginationResponse(shipments);
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

    @PatchMapping("/{shipmentId}/confirm-pickup")
    public ShipmentResponse confirmPickup(@PathVariable Long shipmentId) {
        Shipment shipment = shipmentService.confirmPickup(shipmentId);

        return ShipmentMapper.toResponse(shipment);
    }

    @PatchMapping("/{shipmentId}/confirm-delivery")
    public ShipmentResponse confirmDelivery(@PathVariable Long shipmentId) {
        Shipment shipment = shipmentService.confirmDelivery(shipmentId);

        return ShipmentMapper.toResponse(shipment);
    }

    @PatchMapping("/{shipmentId}/cancel")
    public ShipmentResponse cancel(@PathVariable Long shipmentId) {
        Shipment shipment = shipmentService.cancel(shipmentId);

        return ShipmentMapper.toResponse(shipment);
    }
}
