package com.mbugajski.logistics.shipment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddressResponse {

    private String street;
    private String buildingNumber;
    private String apartmentNumber;
    private String city;
    private String postalCode;
    private String country;
}
