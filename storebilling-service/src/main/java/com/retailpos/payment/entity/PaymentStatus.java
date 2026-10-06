package com.retailpos.payment.entity;

/**
 * Only {@code COMPLETED} is modeled/used this phase - refund/reversal statuses are Phase 6 scope
 * (per the vision doc's own phase ordering) and are intentionally not invented here since their
 * exact semantics haven't been specified yet. {@link PaymentTransaction} is still a full mutable
 * {@code BaseAuditableEntity} (not append-only) precisely so a future phase can transition
 * {@code status} in place without a schema change.
 */
public enum PaymentStatus {
    COMPLETED
}
