package com.mbugajski.logistics.shipment.dto.response;

import com.mbugajski.logistics.shipment.entity.ShipmentStatus;import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ShipmentResponse {

    private Long id;

    private Long customerId;
    private String customerFirstName;
    private String customerLastName;

    private AddressResponse pickupAddress;
    private AddressResponse deliveryAddress;

    private BigDecimal weight;
    private BigDecimal price;

    private ShipmentStatus status;
    private LocalDateTime createdAt;
}
