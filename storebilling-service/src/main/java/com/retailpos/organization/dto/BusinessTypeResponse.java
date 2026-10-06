package com.retailpos.organization.dto;

public record BusinessTypeResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active
) {
}
