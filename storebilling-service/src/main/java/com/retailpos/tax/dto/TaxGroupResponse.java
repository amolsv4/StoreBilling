package com.retailpos.tax.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TaxGroupResponse(
        Long id,
        Long organizationId,
        String organizationCode,
        String organizationName,
        String code,
        String name,
        String description,
        List<TaxComponentResponse> components,
        BigDecimal totalPercentage,
        boolean active,
        boolean inclusive,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
