package com.retailpos.tax.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateTaxGroupRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        List<Long> componentIds,
        Boolean active,
        Boolean inclusive
) {
}
