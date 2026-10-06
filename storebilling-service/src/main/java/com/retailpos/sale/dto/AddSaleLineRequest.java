package com.retailpos.sale.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * {@code unitPrice} is optional - if null, it is resolved via {@code PricingResolutionService};
 * if provided and it differs from the resolved price by more than 0.01 (or nothing resolved at
 * all), the caller needs {@code SALES_PRICE_OVERRIDE} authority.
 */
public record AddSaleLineRequest(
        @NotNull Long productId,
        @NotNull @DecimalMin(value = "0.001", message = "quantity must be greater than zero") BigDecimal quantity,
        BigDecimal unitPrice,
        @DecimalMin(value = "0", message = "discountAmount cannot be negative") BigDecimal discountAmount
) {
}
