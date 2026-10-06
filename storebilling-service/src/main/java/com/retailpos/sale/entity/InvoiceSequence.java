package com.retailpos.sale.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One row per store, holding the next invoice sequence number to assign. PK = {@code store_id}.
 * Deliberately minimal (no {@code BaseAuditableEntity}, no {@code @Version}) - concurrency is
 * handled by an explicit {@code PESSIMISTIC_WRITE} row lock (see
 * {@code InvoiceSequenceRepository}) rather than optimistic-locking retries, and a missing row is
 * self-healed on first use via an atomic upsert-no-op, so there is no meaningful "created"/
 * "updated" audit trail distinct from the counter value itself.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "invoice_sequence")
public class InvoiceSequence {

    @Id
    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "next_value", nullable = false)
    private Long nextValue;
}
