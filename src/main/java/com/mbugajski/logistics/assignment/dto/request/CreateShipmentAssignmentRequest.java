package com.mbugajski.logistics.assignment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CreateShipmentAssignmentRequest {

    @NotNull(message = "Courier ID cannot be null.")
    @Positive(message = "Courier ID must be positive.")
    private Long courierId;

    @NotNull(message = "Vehicle ID cannot be null.")
    @Positive(message = "Vehicle ID must be positive.")
    private Long vehicleId;
}
