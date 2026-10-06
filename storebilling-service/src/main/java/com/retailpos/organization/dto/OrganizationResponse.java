package com.retailpos.organization.dto;

import java.time.LocalDateTime;

public record OrganizationResponse(
        Long id,
        String code,
        String name,
        String legalName,
        String taxRegistrationNo,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String postalCode,
        String contactEmail,
        String contactPhone,
        boolean active,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
