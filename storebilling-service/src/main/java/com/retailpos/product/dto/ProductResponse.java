package com.retailpos.product.dto;

import com.retailpos.product.entity.UnitOfMeasure;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long organizationId,
        String organizationCode,
        String organizationName,
        Long categoryId,
        String categoryCode,
        String categoryName,
        Long taxGroupId,
        String taxGroupCode,
        String taxGroupName,
        String sku,
        String barcode,
        String name,
        String description,
        String brand,
        UnitOfMeasure uom,
        boolean saleEnabled,
        boolean purchaseEnabled,
        boolean active,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
