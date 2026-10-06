package com.retailpos.stocktake.entity;

/** Lifecycle of a {@link StockTake}: mutable while {@code DRAFT}; immutable once {@code COMPLETED}. */
public enum StockTakeStatus {
    DRAFT,
    COMPLETED
}
