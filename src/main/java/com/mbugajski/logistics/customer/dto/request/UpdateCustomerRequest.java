package com.mbugajski.logistics.customer.dto.request;

import com.mbugajski.logistics.address.dto.request.CreateAddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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

    @Email(message = "Wrong email format.")
    @Pattern(regexp = ".*\\S.*", message = "Email cannot be blank.")
    private String email;

    @Pattern(regexp = ".*\\S.*", message = "Phone number cannot be blank.")
    private String phoneNumber;

    @Valid
    private CreateAddressRequest address;

    public boolean hasNoUpdates() {
        return firstName == null
                && lastName == null
                && email == null
                && phoneNumber == null
                && address == null;
    }

}
