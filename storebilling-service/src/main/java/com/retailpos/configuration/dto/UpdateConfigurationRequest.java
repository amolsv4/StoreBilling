package com.retailpos.configuration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Updates value/description/active only - config_key/scope_type/scope_id are immutable after creation. */
public record UpdateConfigurationRequest(
        @NotBlank @Size(max = 1000) String configValue,
        @Size(max = 500) String description,
        @NotNull Boolean active
) {
}
