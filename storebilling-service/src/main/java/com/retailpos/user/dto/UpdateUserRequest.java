package com.retailpos.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Phase 1 simplification: no password-change field here. Rotating a user's own credentials is
 * left to a later phase (e.g. a dedicated /change-password endpoint); this endpoint only manages
 * profile/access/status fields.
 */
public record UpdateUserRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 30) String phone,
        Long organizationId,
        @NotNull Boolean allStoreAccess,
        @NotNull Boolean active,
        @NotNull Boolean accountLocked,
        List<Long> storeIds,
        List<String> roleCodes
) {
}
