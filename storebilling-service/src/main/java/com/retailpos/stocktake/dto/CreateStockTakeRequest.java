package com.retailpos.stocktake.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateStockTakeRequest(
        @NotNull Long organizationId,
        @NotNull Long storeId,
        @NotBlank @Size(max = 30) String stockTakeNumber,
        @NotNull LocalDate stockTakeDate
) {
}
