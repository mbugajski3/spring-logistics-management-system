package com.mbugajski.logistics.customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCustomerRequest {

    @Pattern(regexp = ".*\\S.*", message = "First name cannot be blank.")
    private String firstName;

    @Pattern(regexp = ".*\\S.*", message = "Last name cannot be blank.")
    private String lastName;

    @Pattern(regexp = ".*\\S.*", message = "Phone number cannot be blank.")
    private String phoneNumber;

    @Valid
    private CreateAddressRequest address;

    public boolean hasNoUpdates() {
        return firstName == null
                && lastName == null
                && phoneNumber == null
                && address == null;
    }

}
