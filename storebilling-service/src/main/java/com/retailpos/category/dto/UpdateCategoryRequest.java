package com.retailpos.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        Long parentCategoryId,
        Boolean active
) {
}
