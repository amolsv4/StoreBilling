package com.retailpos.tax.dto;

import com.retailpos.tax.entity.TaxType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TaxComponentResponse(
        Long id,
        Long organizationId,
        String organizationCode,
        String organizationName,
        String code,
        String name,
        TaxType taxType,
        BigDecimal percentage,
        boolean active,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
