package com.retailpos.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateCustomerRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 20) String mobile,
        @Email @Size(max = 150) String email,
        @Size(max = 150) String addressLine1,
        @Size(max = 150) String addressLine2,
        @Size(max = 100) String city,
        @Size(max = 100) String state,
        @Size(max = 100) String country,
        @Size(max = 20) String postalCode,
        @Size(max = 50) String taxRegistrationNo,
        @PositiveOrZero BigDecimal creditLimit,
        Boolean active
) {
}
