package com.retailpos.configuration.dto;

import com.retailpos.configuration.entity.DataType;
import com.retailpos.configuration.entity.ScopeType;

import java.time.LocalDateTime;

public record ConfigurationResponse(
        Long id,
        String configKey,
        String configValue,
        DataType dataType,
        ScopeType scopeType,
        Long scopeId,
        String description,
        boolean active,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
