-- Stock transfer header, moving stock between two stores of the same organization.
-- Deliberately only DRAFT/COMPLETED - no IN_TRANSIT state - so both legs of the transfer post
-- atomically in a single complete() call.
CREATE TABLE stock_transfer
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    organization_id BIGINT      NOT NULL,
    transfer_number VARCHAR(30) NOT NULL,
    from_store_id   BIGINT      NOT NULL,
    to_store_id     BIGINT      NOT NULL,
    transfer_date   DATE        NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by      VARCHAR(50) NOT NULL,
    created_date    DATETIME(6) NOT NULL,
    updated_by      VARCHAR(50) NULL,
    updated_date    DATETIME(6) NULL,
    version         BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_stock_transfer_organization_number UNIQUE (organization_id, transfer_number),
    CONSTRAINT fk_stock_transfer_organization FOREIGN KEY (organization_id) REFERENCES organization (id),
    CONSTRAINT fk_stock_transfer_from_store FOREIGN KEY (from_store_id) REFERENCES store (id),
    CONSTRAINT fk_stock_transfer_to_store FOREIGN KEY (to_store_id) REFERENCES store (id),
    CONSTRAINT chk_stock_transfer_status CHECK (status IN ('DRAFT', 'COMPLETED')),
    CONSTRAINT chk_stock_transfer_different_stores CHECK (from_store_id <> to_store_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_stock_transfer_organization_id ON stock_transfer (organization_id);
CREATE INDEX idx_stock_transfer_from_store_id ON stock_transfer (from_store_id);
CREATE INDEX idx_stock_transfer_to_store_id ON stock_transfer (to_store_id);

-- Stock transfer line: managed via incremental per-line endpoints, same shape as purchase_receipt_line.
CREATE TABLE stock_transfer_line
(
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    stock_transfer_id BIGINT         NOT NULL,
    product_id        BIGINT         NOT NULL,
    quantity          DECIMAL(12, 3) NOT NULL,
    created_by        VARCHAR(50)    NOT NULL,
    created_date      DATETIME(6)    NOT NULL,
    updated_by        VARCHAR(50)    NULL,
    updated_date      DATETIME(6)    NULL,
    version           BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_stock_transfer_line_transfer FOREIGN KEY (stock_transfer_id) REFERENCES stock_transfer (id),
    CONSTRAINT fk_stock_transfer_line_product FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_stock_transfer_line_transfer_id ON stock_transfer_line (stock_transfer_id);
CREATE INDEX idx_stock_transfer_line_product_id ON stock_transfer_line (product_id);
