package com.mbugajski.logistics.courier.mapper;

import com.mbugajski.logistics.courier.dto.response.CourierResponse;
import com.mbugajski.logistics.courier.entity.Courier;

public class CourierMapper {

    public static CourierResponse toResponse(Courier courier) {
        return new CourierResponse(
                courier.getId(),
                courier.getFirstName(),
                courier.getLastName(),
                courier.getPhoneNumber(),
                courier.isActive(),
                courier.isAvailable());
    }
}
