package com.retailpos.category.dto;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        Long organizationId,
        String organizationCode,
        String organizationName,
        String code,
        String name,
        String description,
        Long parentCategoryId,
        String parentCategoryCode,
        String parentCategoryName,
        boolean active,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
