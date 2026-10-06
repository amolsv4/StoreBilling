package com.retailpos.inventory.dto;

import com.retailpos.inventory.entity.StockAdjustmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockAdjustmentResponse(
        Long id,
        Long storeId,
        String storeCode,
        String storeName,
        Long productId,
        String productSku,
        String productName,
        StockAdjustmentType adjustmentType,
        BigDecimal quantityChange,
        BigDecimal balanceAfter,
        String reason,
        String createdBy,
        LocalDateTime createdDate
) {
}
