package com.retailpos.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        Long organizationId,
        String organizationCode,
        String organizationName,
        String code,
        String name,
        String mobile,
        String email,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String postalCode,
        String taxRegistrationNo,
        BigDecimal creditLimit,
        int loyaltyPoints,
        boolean active,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
