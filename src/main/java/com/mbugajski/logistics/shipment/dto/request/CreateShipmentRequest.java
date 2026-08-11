package com.mbugajski.logistics.shipment.dto.request;

import com.mbugajski.logistics.address.dto.request.CreateAddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CreateShipmentRequest {

    @Positive(message = "Customer id must be positive.")
    @NotNull(message = "Customer id cannot be null.")
    private Long customerId;

    @NotNull(message = "Pickup address cannot be null.")
    @Valid
    private CreateAddressRequest pickupAddress;

    @NotNull(message = "Delivery address cannot be null.")
    @Valid
    private CreateAddressRequest deliveryAddress;

    @NotNull(message = "Weight cannot be null.")
    @DecimalMin(value = "0.01", message = "Weight must be greater than 0.")
    @DecimalMax(value = "20.00", message = "Weight cannot exceed 20 kg.")
    private BigDecimal weight;
}
