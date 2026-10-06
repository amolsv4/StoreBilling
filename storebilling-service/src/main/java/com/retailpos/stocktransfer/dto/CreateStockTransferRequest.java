package com.retailpos.stocktransfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateStockTransferRequest(
        @NotNull Long organizationId,
        @NotBlank @Size(max = 30) String transferNumber,
        @NotNull Long fromStoreId,
        @NotNull Long toStoreId,
        @NotNull LocalDate transferDate
) {
}
