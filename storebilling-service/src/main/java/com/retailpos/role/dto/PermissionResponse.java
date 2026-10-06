package com.retailpos.role.dto;

public record PermissionResponse(
        Long id,
        String code,
        String name,
        String module,
        String description
) {
}
