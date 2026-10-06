-- Stock adjustment: append-only, single-row correction (no header+lines - a correction is one
-- product/store/reason event, not a multi-line document). Same shape as audit_log/
-- inventory_transaction - no updated_by/updated_date/version; there is no update/delete endpoint
-- for this entity at all.
CREATE TABLE stock_adjustment
(
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    store_id        BIGINT         NOT NULL,
    product_id      BIGINT         NOT NULL,
    adjustment_type VARCHAR(20)    NOT NULL,
    quantity_change DECIMAL(12, 3) NOT NULL,
    reason          VARCHAR(500)   NOT NULL,
    created_by      VARCHAR(50)    NOT NULL,
    created_date    DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_stock_adjustment_store FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT fk_stock_adjustment_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT chk_stock_adjustment_type CHECK (adjustment_type IN ('ADJUSTMENT', 'DAMAGE', 'EXPIRY'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_stock_adjustment_store_id ON stock_adjustment (store_id);
CREATE INDEX idx_stock_adjustment_product_id ON stock_adjustment (product_id);
CREATE INDEX idx_stock_adjustment_created_date ON stock_adjustment (created_date);
