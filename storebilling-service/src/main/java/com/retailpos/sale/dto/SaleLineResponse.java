package com.retailpos.sale.dto;

import com.retailpos.product.entity.UnitOfMeasure;

import java.math.BigDecimal;

public record SaleLineResponse(
        Long id,
        Long productId,
        String productName,
        String sku,
        UnitOfMeasure uom,
        BigDecimal quantity,
        BigDecimal unitPrice,
        boolean priceOverridden,
        BigDecimal discountAmount,
        Long taxGroupId,
        BigDecimal taxPercentage,
        BigDecimal taxAmount,
        BigDecimal lineTotal
) {
}
