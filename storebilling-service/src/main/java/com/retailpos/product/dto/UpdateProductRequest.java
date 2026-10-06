package com.retailpos.product.dto;

import com.retailpos.product.entity.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
        @NotNull Long categoryId,
        Long taxGroupId,
        @Size(max = 50) String barcode,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        @Size(max = 100) String brand,
        @NotNull UnitOfMeasure uom,
        Boolean saleEnabled,
        Boolean purchaseEnabled,
        Boolean active
) {
}
