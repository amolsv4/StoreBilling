package com.retailpos.configuration.dto;

import com.retailpos.configuration.entity.DataType;
import com.retailpos.configuration.entity.ScopeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateConfigurationRequest(
        @NotBlank @Size(max = 100) String configKey,
        @NotBlank @Size(max = 1000) String configValue,
        @NotNull DataType dataType,
        @NotNull ScopeType scopeType,
        /*
         * Required even for GLOBAL scope - callers must pass the sentinel 0
         * (see AppConfiguration.GLOBAL_SCOPE_ID) rather than leaving it null.
         */
        @NotNull Long scopeId,
        @Size(max = 500) String description
) {
}
