package com.retailpos.sale.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSaleRequest(
        @NotNull Long storeId,
        @NotNull Long terminalId,
        Long customerId,
        @Size(max = 500) String notes
) {
}
