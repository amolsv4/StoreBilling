package com.retailpos.inventory.service;

import java.math.BigDecimal;

/**
 * Single choke point for every stock mutation in the system - appends one
 * {@code inventory_transaction} row and upserts the {@code inventory} balance row for
 * {@code (productId, storeId)}.
 *
 * <p>Runs under Spring's default REQUIRED propagation: joins the caller's existing transaction
 * when called from one (e.g. Sales' completion flow, so a stock-mutation failure rolls back the
 * whole sale) or starts its own when called standalone (e.g. a controller-triggered adjustment).
 *
 * <p>Called by: purchase-receipt receive, stock-transfer complete (twice per line - once per
 * store), stock-take complete, stock-adjustment create, and by Sales on sale completion
 * (type=SALE, negative quantityChange).
 *
 * <p>Never throws for insufficient stock - negative balances are allowed (logged at WARN), since
 * rejecting here would force every caller, including Sales mid-checkout, to catch a business
 * exception and decide whether to abort its own operation. A negative balance is a reconciliation
 * signal (handled by Stock Take), not a hard stop.
 */
public interface InventoryService {

    /**
     * @param productId      required
     * @param storeId        required
     * @param quantityChange signed delta; positive = stock in, negative = stock out
     * @param type           ledger transaction type
     * @param referenceType  polymorphic pointer discriminator, e.g. "SALE" (nullable)
     * @param referenceId    id in whichever table referenceType names (nullable)
     * @param notes          optional free-text context (nullable)
     */
    StockChangeResult applyStockChange(Long productId,
                                        Long storeId,
                                        BigDecimal quantityChange,
                                        InventoryTransactionType type,
                                        String referenceType,
                                        Long referenceId,
                                        String notes);

    /** Read-only current balance (zero if no row exists yet for this product/store pair). */
    BigDecimal getCurrentStock(Long productId, Long storeId);
}
