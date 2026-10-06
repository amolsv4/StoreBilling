package com.retailpos.auth.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInMs,
        Long userId,
        String username,
        List<String> roles,
        Long organizationId,
        boolean allStoreAccess,
        List<Long> storeAccess
) {
}
