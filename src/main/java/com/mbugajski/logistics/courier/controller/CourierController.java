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

    @PostMapping
    public ResponseEntity<CourierResponse> create(@RequestBody @Valid CreateCourierRequest courierRequest) {
        Courier courier = courierService.create(courierRequest);
        CourierResponse courierResponse = CourierMapper.toResponse(courier);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(courierResponse);
    }
}
