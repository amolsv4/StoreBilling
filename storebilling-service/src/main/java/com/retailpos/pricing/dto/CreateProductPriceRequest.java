package com.retailpos.pricing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProductPriceRequest(
        @NotNull Long priceListId,
        @NotNull Long productId,
        @NotNull @PositiveOrZero BigDecimal basePrice,
        @NotNull @PositiveOrZero BigDecimal sellingPrice,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
