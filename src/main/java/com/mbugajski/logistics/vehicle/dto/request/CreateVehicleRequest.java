package com.mbugajski.logistics.vehicle.dto.request;

import com.mbugajski.logistics.vehicle.entity.VehicleType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CreateVehicleRequest {

    @NotBlank(message = "Brand name cannot be null or empty.")
    private String brand;

    @NotBlank(message = "Model name cannot be null or empty.")
    private String model;

    @NotBlank(message = "Registration number cannot be null or empty.")
    private String registrationNumber;

    @NotNull(message = "Vehicle type cannot be null.")
    private VehicleType vehicleType;

    @NotNull(message = "Maximum load cannot be null.")
    @DecimalMin(value = "0.01", message = "Maximum load must be more than 0 kg.")
    @DecimalMax(value = "1600.00", message = "Maximum load cannot be more than 1600 kg.")
    private BigDecimal maximumLoad;
}
