package com.mbugajski.logistics.courier.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourierResponse {
    Long id;
    String firstName;
    String lastName;
    String phoneNumber;
    boolean active;
    boolean available;
}
