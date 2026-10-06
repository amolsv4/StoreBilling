package com.retailpos.tax.dto;

import com.retailpos.tax.entity.TaxType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTaxComponentRequest(
        @NotNull Long organizationId,
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 150) String name,
        @NotNull TaxType taxType,
        @NotNull @PositiveOrZero @DecimalMax("100.00") BigDecimal percentage
) {
}
