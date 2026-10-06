package com.retailpos.stocktake.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateStockTakeLineRequest(
        @NotNull @DecimalMin(value = "0.000") BigDecimal countedQuantity
) {
}
