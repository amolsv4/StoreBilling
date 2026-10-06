package com.retailpos.stocktransfer.dto;

import java.math.BigDecimal;

public record StockTransferLineResponse(
        Long id,
        Long productId,
        String productSku,
        String productName,
        BigDecimal quantity
) {
}
