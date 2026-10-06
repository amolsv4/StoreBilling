package com.retailpos.pricing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductPriceResponse(
        Long id,
        Long priceListId,
        String priceListCode,
        String priceListName,
        Long productId,
        String productSku,
        String productName,
        BigDecimal basePrice,
        BigDecimal sellingPrice,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
