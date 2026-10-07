package com.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillingAddressRequest {

    private Boolean sameAsShipping = true;
    private String fullName;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
