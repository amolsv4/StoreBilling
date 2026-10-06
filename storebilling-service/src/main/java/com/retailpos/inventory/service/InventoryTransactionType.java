package com.retailpos.inventory.service;

/**
 * Ledger transaction types recorded by {@link InventoryService#applyStockChange}.
 *
 * <p>{@code PURCHASE_RETURN}/{@code SALE_RETURN} are reserved values only - Phase 3 never posts
 * them (returns are Phase 6 scope per the vision doc's own phase ordering).
 */
public enum InventoryTransactionType {
    PURCHASE,
    PURCHASE_RETURN,
    SALE,
    SALE_RETURN,
    TRANSFER_IN,
    TRANSFER_OUT,
    ADJUSTMENT,
    DAMAGE,
    EXPIRY,
    STOCK_TAKE
}
