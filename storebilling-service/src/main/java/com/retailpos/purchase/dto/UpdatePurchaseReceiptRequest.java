package com.retailpos.purchase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** No {@code status} field - status only ever changes via the dedicated {@code /receive} action. */
public record UpdatePurchaseReceiptRequest(
        @NotBlank @Size(max = 150) String supplierName,
        @NotBlank @Size(max = 30) String receiptNumber,
        @NotNull LocalDate receiptDate
) {
}
