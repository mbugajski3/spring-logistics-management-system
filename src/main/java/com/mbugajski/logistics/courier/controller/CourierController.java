package com.mbugajski.logistics.courier.controller;

import com.mbugajski.logistics.courier.dto.request.CreateCourierRequest;
import com.mbugajski.logistics.courier.dto.response.CourierResponse;
import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.courier.mapper.CourierMapper;
import com.mbugajski.logistics.courier.service.CourierService;
import com.mbugajski.logistics.shipment.entity.Shipment;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/couriers")
public class CourierController {

    private final CourierService courierService;

    public CourierController(CourierService courierService) {
        this.courierService = courierService;
    }

    @GetMapping
    public List<CourierResponse> findAll() {
        return courierService.findAll()
                .stream()
                .map(CourierMapper::toResponse)
                .toList();
    }


    @PostMapping
    public ResponseEntity<CourierResponse> create(@RequestBody @Valid CreateCourierRequest courierRequest) {
        Courier courier = courierService.create(courierRequest);
        CourierResponse courierResponse = CourierMapper.toResponse(courier);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(courierResponse);
    }

    @GetMapping("/{courierId}")
    public CourierResponse findById(@PathVariable Long courierId) {
        Courier courier = courierService.findById(courierId);

        return CourierMapper.toResponse(courier);
    }

    @PatchMapping("/{courierId}/busy")
    public CourierResponse markAsBusy(@PathVariable Long courierId) {
        Courier courier = courierService.markAsBusy(courierId);

        return CourierMapper.toResponse(courier);
    }

    @PatchMapping("/{courierId}/available")
    public CourierResponse markAsAvailable(@PathVariable Long courierId) {
        Courier courier = courierService.markAsAvailable(courierId);

        return CourierMapper.toResponse(courier);
    }

    @PatchMapping("/{courierId}/deactivate")
    public CourierResponse deactivate(@PathVariable Long courierId) {
        Courier courier = courierService.deactivate(courierId);

        return CourierMapper.toResponse(courier);
    }

    @PatchMapping("/{courierId}/activate")
    public CourierResponse activate(@PathVariable Long courierId) {
        Courier courier = courierService.activate(courierId);

        return CourierMapper.toResponse(courier);
    }
}
