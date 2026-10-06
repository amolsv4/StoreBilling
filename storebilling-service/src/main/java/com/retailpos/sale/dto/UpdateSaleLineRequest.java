package com.retailpos.sale.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

/** Any field left null leaves the corresponding line value unchanged. */
public record UpdateSaleLineRequest(
        @DecimalMin(value = "0.001", message = "quantity must be greater than zero") BigDecimal quantity,
        BigDecimal unitPrice,
        @DecimalMin(value = "0", message = "discountAmount cannot be negative") BigDecimal discountAmount
) {
}
