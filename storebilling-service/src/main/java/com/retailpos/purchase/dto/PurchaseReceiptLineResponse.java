package com.retailpos.purchase.dto;

import java.math.BigDecimal;

public record PurchaseReceiptLineResponse(
        Long id,
        Long productId,
        String productSku,
        String productName,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal lineTotal
) {
}
