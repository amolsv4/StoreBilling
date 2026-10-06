package com.retailpos.sale.dto;

import java.math.BigDecimal;

/** {@code changeDue = amountPaid - sale.totalAmount}; overpayment is allowed. */
public record CompleteSaleResponse(
        SaleResponse sale,
        BigDecimal amountPaid,
        BigDecimal changeDue
) {
}
