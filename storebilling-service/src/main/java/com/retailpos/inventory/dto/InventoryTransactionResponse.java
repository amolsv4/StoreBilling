package com.retailpos.inventory.dto;

import com.retailpos.inventory.service.InventoryTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryTransactionResponse(
        Long id,
        Long productId,
        String productSku,
        String productName,
        Long storeId,
        String storeCode,
        String storeName,
        InventoryTransactionType transactionType,
        BigDecimal quantityChange,
        BigDecimal balanceAfter,
        String referenceType,
        Long referenceId,
        String notes,
        String createdBy,
        LocalDateTime createdDate
) {
}
