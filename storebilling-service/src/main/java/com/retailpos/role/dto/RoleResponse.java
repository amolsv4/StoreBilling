package com.retailpos.role.dto;

import java.util.List;

public record RoleResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean systemRole,
        List<PermissionResponse> permissions
) {
}
