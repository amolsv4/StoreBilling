package com.retailpos.stocktake.dto;

import com.retailpos.stocktake.entity.StockTakeStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StockTakeResponse(
        Long id,
        Long organizationId,
        Long storeId,
        String storeCode,
        String storeName,
        String stockTakeNumber,
        LocalDate stockTakeDate,
        StockTakeStatus status,
        List<StockTakeLineResponse> lines,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
