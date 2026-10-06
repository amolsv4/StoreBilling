package com.retailpos.sale.entity;

/**
 * Sale lifecycle. Deliberately has no {@code CANCELLED} value this phase (Phase 6 scope per the
 * vision doc's own phase ordering) - an abandoned {@code DRAFT} sale simply has no void action.
 */
public enum SaleStatus {
    DRAFT,
    HELD,
    COMPLETED
}
