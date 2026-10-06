package com.retailpos.sale.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Bill-level discount, separate from any line-level discounts already baked into line totals. */
public record UpdateSaleDiscountRequest(
        @NotNull @DecimalMin(value = "0", message = "discountAmount cannot be negative") BigDecimal discountAmount
) {
}
