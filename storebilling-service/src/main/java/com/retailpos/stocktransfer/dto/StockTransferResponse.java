package com.retailpos.stocktransfer.dto;

import com.retailpos.stocktransfer.entity.StockTransferStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StockTransferResponse(
        Long id,
        Long organizationId,
        String transferNumber,
        Long fromStoreId,
        String fromStoreCode,
        String fromStoreName,
        Long toStoreId,
        String toStoreCode,
        String toStoreName,
        LocalDate transferDate,
        StockTransferStatus status,
        List<StockTransferLineResponse> lines,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
