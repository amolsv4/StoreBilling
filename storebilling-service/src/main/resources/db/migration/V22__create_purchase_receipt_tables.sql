-- Purchase receipt header. No structured Supplier master exists yet (or is justified by this
-- phase's scope) - supplier_name is deliberately free text. Mutable (header and lines) while
-- DRAFT; immutable once RECEIVED - a mistake afterward is corrected by a new stock_adjustment,
-- never by editing history.
CREATE TABLE purchase_receipt
(
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    organization_id BIGINT         NOT NULL,
    store_id        BIGINT         NOT NULL,
    supplier_name   VARCHAR(150)   NOT NULL,
    receipt_number  VARCHAR(30)    NOT NULL,
    receipt_date    DATE           NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    total_amount    DECIMAL(14, 2) NOT NULL DEFAULT 0,
    created_by      VARCHAR(50)    NOT NULL,
    created_date    DATETIME(6)    NOT NULL,
    updated_by      VARCHAR(50)    NULL,
    updated_date    DATETIME(6)    NULL,
    version         BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_purchase_receipt_organization_number UNIQUE (organization_id, receipt_number),
    CONSTRAINT fk_purchase_receipt_organization FOREIGN KEY (organization_id) REFERENCES organization (id),
    CONSTRAINT fk_purchase_receipt_store FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT chk_purchase_receipt_status CHECK (status IN ('DRAFT', 'RECEIVED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_purchase_receipt_organization_id ON purchase_receipt (organization_id);
CREATE INDEX idx_purchase_receipt_store_id ON purchase_receipt (store_id);

-- Purchase receipt line: managed via incremental per-line endpoints (add/update/remove one at a
-- time), not a bulk header+lines payload - mirrors Sales' scan-by-scan cart model.
CREATE TABLE purchase_receipt_line
(
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    purchase_receipt_id BIGINT         NOT NULL,
    product_id          BIGINT         NOT NULL,
    quantity            DECIMAL(12, 3) NOT NULL,
    unit_cost           DECIMAL(12, 2) NOT NULL,
    line_total          DECIMAL(14, 2) NOT NULL,
    created_by          VARCHAR(50)    NOT NULL,
    created_date        DATETIME(6)    NOT NULL,
    updated_by          VARCHAR(50)    NULL,
    updated_date        DATETIME(6)    NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_purchase_receipt_line_receipt FOREIGN KEY (purchase_receipt_id) REFERENCES purchase_receipt (id),
    CONSTRAINT fk_purchase_receipt_line_product FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_purchase_receipt_line_receipt_id ON purchase_receipt_line (purchase_receipt_id);
CREATE INDEX idx_purchase_receipt_line_product_id ON purchase_receipt_line (product_id);
