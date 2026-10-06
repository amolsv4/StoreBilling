package com.retailpos.purchase.entity;

/**
 * Lifecycle of a {@link PurchaseReceipt}: fully editable/hard-deletable while {@code DRAFT}
 * (nothing has touched the ledger yet); immutable once {@code RECEIVED} - a mistake afterward is
 * corrected by a new {@code stock_adjustment}, never by editing history.
 */
public enum PurchaseReceiptStatus {
    DRAFT,
    RECEIVED
}
