package com.retailpos.sale.dto;

import com.retailpos.sale.entity.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Every mutating Sale endpoint returns this full, server-computed shape - the frontend never
 * recomputes tax/discount business logic itself, only re-renders server truth.
 */
public record SaleResponse(
        Long id,
        Long organizationId,
        Long storeId,
        String storeCode,
        Long terminalId,
        String terminalCode,
        Long customerId,
        String customerName,
        Long soldByUserId,
        String soldByUsername,
        LocalDateTime saleDate,
        SaleStatus status,
        String invoiceNumber,
        String currency,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String notes,
        LocalDateTime completedDate,
        List<SaleLineResponse> lines,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
