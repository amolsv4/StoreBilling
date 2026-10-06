package com.retailpos.store.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStoreRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 150) String addressLine1,
        @Size(max = 150) String addressLine2,
        @Size(max = 100) String city,
        @Size(max = 100) String state,
        @Size(max = 100) String country,
        @Size(max = 20) String postalCode,
        @Size(max = 30) String phone,
        @Email @Size(max = 150) String email,
        @Size(max = 50) String timezone,
        Boolean active,
        Long defaultPriceListId
) {
}
