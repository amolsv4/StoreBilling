package com.retailpos.stocktake.dto;

import java.math.BigDecimal;

public record StockTakeLineResponse(
        Long id,
        Long productId,
        String productSku,
        String productName,
        BigDecimal systemQuantity,
        BigDecimal countedQuantity,
        BigDecimal variance
) {
}
