package com.retailpos.store.dto;

import java.time.LocalDateTime;

public record StoreResponse(
        Long id,
        Long organizationId,
        String organizationCode,
        Long businessTypeId,
        String businessTypeCode,
        String code,
        String name,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String postalCode,
        String phone,
        String email,
        String timezone,
        boolean active,
        Long defaultPriceListId,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
