package com.retailpos.stocktake.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** No {@code systemQuantity} field - it is snapshotted server-side, never client-supplied. */
public record CreateStockTakeLineRequest(
        @NotNull Long productId,
        @NotNull @DecimalMin(value = "0.000") BigDecimal countedQuantity
) {
}
