package com.retailpos.inventory.entity;

/**
 * Reason category for a {@link StockAdjustment}. Mirrors three of the
 * {@link com.retailpos.inventory.service.InventoryTransactionType} constants by name so a
 * {@code StockAdjustment} can be posted to the ledger via a direct {@code valueOf} translation.
 */
public enum StockAdjustmentType {
    ADJUSTMENT,
    DAMAGE,
    EXPIRY
}
