package com.retailpos.user.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        Long organizationId,
        boolean allStoreAccess,
        boolean active,
        boolean accountLocked,
        LocalDateTime lastLoginDate,
        List<String> roleCodes,
        List<Long> storeIds,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
