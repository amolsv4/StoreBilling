package com.retailpos.purchase.dto;

import com.retailpos.purchase.entity.PurchaseReceiptStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Every mutating endpoint (header create/update, line add/update/remove, receive) returns this
 * full, refreshed representation - lines plus the server-recomputed {@code totalAmount} - mirroring
 * the Sales module's "the caller never recomputes business logic itself" contract.
 */
public record PurchaseReceiptResponse(
        Long id,
        Long organizationId,
        Long storeId,
        String storeCode,
        String storeName,
        String supplierName,
        String receiptNumber,
        LocalDate receiptDate,
        PurchaseReceiptStatus status,
        BigDecimal totalAmount,
        List<PurchaseReceiptLineResponse> lines,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
