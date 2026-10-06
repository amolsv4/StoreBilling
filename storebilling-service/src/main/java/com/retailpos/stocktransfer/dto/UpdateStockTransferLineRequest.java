package com.retailpos.stocktransfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateStockTransferLineRequest(
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantity
) {
}
