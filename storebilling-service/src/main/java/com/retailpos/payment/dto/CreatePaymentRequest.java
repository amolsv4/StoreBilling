package com.retailpos.payment.dto;

import com.retailpos.payment.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull PaymentMethod paymentMethod,
        @NotNull @DecimalMin(value = "0.01", message = "amount must be greater than zero") BigDecimal amount,
        @Size(max = 100) String referenceNumber
) {
}
