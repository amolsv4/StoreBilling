package com.retailpos.stocktransfer.entity;

/**
 * Lifecycle of a {@link StockTransfer} - deliberately only {@code DRAFT}/{@code COMPLETED}, no
 * {@code IN_TRANSIT} state, so both legs of the transfer (source decrement + destination
 * increment) post atomically in a single {@code complete()} call rather than spanning two
 * separately-committed steps.
 */
public enum StockTransferStatus {
    DRAFT,
    COMPLETED
}
