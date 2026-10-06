package com.retailpos.pricing.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PriceListResponse(
        Long id,
        Long organizationId,
        String organizationCode,
        String organizationName,
        String code,
        String name,
        String description,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
