-- Invoice sequence: one row per store, holding the next invoice sequence number to assign.
-- PK = store_id (one sequence per store). The counter never resets - the date segment in the
-- formatted invoice number ({storeCode}-{yyyyMMdd}-{sequence:06d}) is for readability only, to
-- avoid midnight-rollover concurrency edge cases. A missing row for a store is self-healed on
-- first use via an atomic upsert-no-op (see InvoiceSequenceRepository) - no seed data needed here.
-- Deliberately minimal (no created_by/created_date/updated_*/version) - concurrency is handled by
-- an explicit PESSIMISTIC_WRITE row lock, not optimistic-locking retries.
CREATE TABLE invoice_sequence
(
    store_id   BIGINT NOT NULL,
    next_value BIGINT NOT NULL DEFAULT 1,
    PRIMARY KEY (store_id),
    CONSTRAINT fk_invoice_sequence_store FOREIGN KEY (store_id) REFERENCES store (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Tax inclusive/exclusive flag on the existing tax_group table (Phase 3 Section 0 item 3).
-- Exclusive (the default/prior behaviour) adds tax on top of the entered price; inclusive treats
-- the entered price as tax-already-included and back-calculates the tax portion for display only
-- - the payable total is unaffected. Branched on by Sales when snapshotting a line's
-- tax_amount/line_total at add-time.
ALTER TABLE tax_group
    ADD COLUMN is_inclusive BOOLEAN NOT NULL DEFAULT FALSE AFTER description;
