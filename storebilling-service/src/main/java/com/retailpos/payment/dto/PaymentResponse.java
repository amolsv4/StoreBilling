package com.retailpos.payment.dto;

import com.retailpos.payment.entity.PaymentMethod;
import com.retailpos.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long saleId,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        String referenceNumber,
        PaymentStatus status,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
