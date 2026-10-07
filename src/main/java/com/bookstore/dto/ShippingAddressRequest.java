package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressRequest {

    @NotBlank(message = "Full name is required for shipping")
    private String fullName;

    @NotBlank(message = "Street address is required for shipping")
    private String streetAddress;

    @NotBlank(message = "City is required for shipping")
    private String city;

    @NotBlank(message = "State is required for shipping")
    private String state;

    @NotBlank(message = "Postal code is a required field for physical shipment delivery.")
    private String postalCode;

    @NotBlank(message = "Country code is required")
    private String country;

    private String phoneNumber;
}
