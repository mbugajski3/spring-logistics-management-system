package com.mbugajski.logistics.vehicle.mapper;

import com.mbugajski.logistics.vehicle.dto.response.VehicleResponse;
import com.mbugajski.logistics.vehicle.entity.Vehicle;

public class VehicleMapper {
    public static VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getRegistrationNumber(),
                vehicle.getVehicleType(),
                vehicle.getMaximumLoad(),
                vehicle.isActive(),
                vehicle.isAvailable()
        );
    }
}
