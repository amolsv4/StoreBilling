package com.retailpos.stocktransfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** No {@code status} field - status only ever changes via the dedicated {@code /complete} action. */
public record UpdateStockTransferRequest(
        @NotBlank @Size(max = 30) String transferNumber,
        @NotNull Long fromStoreId,
        @NotNull Long toStoreId,
        @NotNull LocalDate transferDate
) {
}
