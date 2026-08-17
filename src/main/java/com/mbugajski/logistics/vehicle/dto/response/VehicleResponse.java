package com.mbugajski.logistics.vehicle.dto.response;

import com.mbugajski.logistics.vehicle.entity.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class VehicleResponse {

    private Long id;
    private String brand;
    private String model;
    private String registrationNumber;
    private VehicleType vehicleType;
    private BigDecimal maximumLoad;
    private boolean active;
    private boolean available;
}
