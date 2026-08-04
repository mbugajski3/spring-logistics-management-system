package com.mbugajski.logistics.customer;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateAddressRequest {

    @NotBlank(message = "Street cannot be blank.")
    private String street;

    @NotBlank(message = "Building number cannot be blank.")
    private String buildingNumber;

    private String apartmentNumber;

    @NotBlank(message = "City cannot be blank.")
    private String city;

    @NotBlank(message = "Postal code cannot be blank.")
    private String postalCode;
    
    @NotBlank(message = "Country cannot be blank.")
    private String country;
}
