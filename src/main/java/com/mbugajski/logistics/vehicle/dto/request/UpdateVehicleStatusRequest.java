package com.mbugajski.logistics.vehicle.dto.request;

import com.mbugajski.logistics.vehicle.entity.VehicleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateVehicleStatusRequest {

    @NotNull(message = "Vehicle status cannot be null.")
    private VehicleStatus status;
}
