package com.mbugajski.logistics.vehicle.controller;

import com.mbugajski.logistics.vehicle.dto.request.CreateVehicleRequest;
import com.mbugajski.logistics.vehicle.dto.request.UpdateVehicleStatusRequest;
import com.mbugajski.logistics.vehicle.dto.response.VehicleResponse;
import com.mbugajski.logistics.vehicle.entity.Vehicle;
import com.mbugajski.logistics.vehicle.mapper.VehicleMapper;
import com.mbugajski.logistics.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public List<VehicleResponse> findAll() {
        return vehicleService.findAll()
                .stream()
                .map(VehicleMapper::toResponse)
                .toList();
    }

    @GetMapping("/{vehicleId}")
    public VehicleResponse findById(@PathVariable Long vehicleId) {
        Vehicle vehicle = vehicleService.findById(vehicleId);

        return VehicleMapper.toResponse(vehicle);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse create(@Valid @RequestBody CreateVehicleRequest vehicleRequest) {
        Vehicle vehicle = vehicleService.create(vehicleRequest);

        return VehicleMapper.toResponse(vehicle);
    }

    @PatchMapping("{vehicleId}/status")
    public VehicleResponse updateStatus(@PathVariable Long vehicleId, @Valid @RequestBody UpdateVehicleStatusRequest statusRequest) {
        Vehicle vehicle = vehicleService.updateStatus(vehicleId, statusRequest);

        return VehicleMapper.toResponse(vehicle);
    }
}
