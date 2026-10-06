package com.retailpos.pricing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePriceListRequest(
        @NotNull Long organizationId,
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
