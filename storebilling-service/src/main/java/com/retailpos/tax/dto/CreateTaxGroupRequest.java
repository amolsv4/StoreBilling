package com.retailpos.tax.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateTaxGroupRequest(
        @NotNull Long organizationId,
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        List<Long> componentIds
) {
}
