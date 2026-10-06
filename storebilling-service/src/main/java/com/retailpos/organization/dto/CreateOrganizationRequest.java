package com.retailpos.organization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 200) String legalName,
        @Size(max = 50) String taxRegistrationNo,
        @Size(max = 150) String addressLine1,
        @Size(max = 150) String addressLine2,
        @Size(max = 100) String city,
        @Size(max = 100) String state,
        @Size(max = 100) String country,
        @Size(max = 20) String postalCode,
        @Email @Size(max = 150) String contactEmail,
        @Size(max = 30) String contactPhone
) {
}
