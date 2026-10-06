package com.retailpos.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product/store display fields ({@code productSku}/{@code productName}/{@code storeCode}/
 * {@code storeName}) are joined in the service layer, not via a JPA association - see
 * {@link com.retailpos.inventory.entity.Inventory}'s javadoc for why {@code productId}/
 * {@code storeId} are plain columns.
 */
public record InventoryResponse(
        Long id,
        Long productId,
        String productSku,
        String productName,
        Long storeId,
        String storeCode,
        String storeName,
        BigDecimal quantityOnHand,
        String updatedBy,
        LocalDateTime updatedDate
) {
}
