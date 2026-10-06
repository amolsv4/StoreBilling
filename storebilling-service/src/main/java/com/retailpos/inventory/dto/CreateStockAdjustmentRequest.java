package com.retailpos.inventory.dto;

import com.retailpos.inventory.entity.StockAdjustmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateStockAdjustmentRequest(
        @NotNull Long storeId,
        @NotNull Long productId,
        @NotNull StockAdjustmentType adjustmentType,
        @NotNull BigDecimal quantityChange,
        @NotBlank @Size(max = 500) String reason
) {
}
