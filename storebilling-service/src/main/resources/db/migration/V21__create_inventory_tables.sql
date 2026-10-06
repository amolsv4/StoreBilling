-- Inventory balance: current stock quantity for a single (product_id, store_id) pair. Custom
-- minimal audit shape - NOT the BaseAuditableEntity shape - a balance row has no meaningful
-- "created" event distinct from its first ledger entry (see inventory_transaction below /
-- InventoryServiceImpl#applyStockChange, which upserts this row from zero on first use), so there
-- is no created_by/created_date column at all, only updated_by/updated_date/version.
CREATE TABLE inventory
(
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    product_id       BIGINT         NOT NULL,
    store_id         BIGINT         NOT NULL,
    quantity_on_hand DECIMAL(14, 3) NOT NULL DEFAULT 0,
    updated_by       VARCHAR(50)    NULL,
    updated_date     DATETIME(6)    NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_inventory_product_store UNIQUE (product_id, store_id),
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_inventory_store FOREIGN KEY (store_id) REFERENCES store (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_inventory_store_id ON inventory (store_id);

-- Inventory transaction: append-only ledger, one row per InventoryService#applyStockChange call.
-- Same shape as audit_log - no updated_by/updated_date/version, since rows are never modified
-- after insert. reference_type/reference_id are a deliberately open-ended, polymorphic pointer
-- (no CHECK constraint, no FK) - the table they name depends on transaction_type (e.g.
-- "PURCHASE_RECEIPT", "SALE", "STOCK_TRANSFER", "STOCK_ADJUSTMENT", "STOCK_TAKE").
CREATE TABLE inventory_transaction
(
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    product_id       BIGINT         NOT NULL,
    store_id         BIGINT         NOT NULL,
    transaction_type VARCHAR(20)    NOT NULL,
    quantity_change  DECIMAL(14, 3) NOT NULL,
    balance_after    DECIMAL(14, 3) NOT NULL,
    reference_type   VARCHAR(30)    NULL,
    reference_id     BIGINT         NULL,
    notes            VARCHAR(500)   NULL,
    created_by       VARCHAR(50)    NOT NULL,
    created_date     DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_inventory_transaction_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_inventory_transaction_store FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT chk_inventory_transaction_type CHECK (transaction_type IN
        ('PURCHASE', 'PURCHASE_RETURN', 'SALE', 'SALE_RETURN', 'TRANSFER_IN', 'TRANSFER_OUT',
         'ADJUSTMENT', 'DAMAGE', 'EXPIRY', 'STOCK_TAKE'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_inventory_transaction_product_store ON inventory_transaction (product_id, store_id);
CREATE INDEX idx_inventory_transaction_reference ON inventory_transaction (reference_type, reference_id);
CREATE INDEX idx_inventory_transaction_created_date ON inventory_transaction (created_date);
