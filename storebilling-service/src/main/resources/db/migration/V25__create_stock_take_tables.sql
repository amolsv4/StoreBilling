-- Stock take (physical inventory count) header for a single store. Mutable while DRAFT;
-- immutable once COMPLETED.
CREATE TABLE stock_take
(
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    organization_id   BIGINT      NOT NULL,
    store_id          BIGINT      NOT NULL,
    stock_take_number VARCHAR(30) NOT NULL,
    stock_take_date   DATE        NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by        VARCHAR(50) NOT NULL,
    created_date      DATETIME(6) NOT NULL,
    updated_by        VARCHAR(50) NULL,
    updated_date      DATETIME(6) NULL,
    version           BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_stock_take_organization_number UNIQUE (organization_id, stock_take_number),
    CONSTRAINT fk_stock_take_organization FOREIGN KEY (organization_id) REFERENCES organization (id),
    CONSTRAINT fk_stock_take_store FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT chk_stock_take_status CHECK (status IN ('DRAFT', 'COMPLETED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_stock_take_organization_id ON stock_take (organization_id);
CREATE INDEX idx_stock_take_store_id ON stock_take (store_id);

-- Stock take line: system_quantity is snapshotted server-side (from the live inventory balance)
-- when the line is added - never client-supplied. variance is recomputed against the live
-- balance again at completion time (see StockTakeServiceImpl#complete) - the value stored here
-- reflects whichever of those two snapshots is most recent.
CREATE TABLE stock_take_line
(
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    stock_take_id    BIGINT         NOT NULL,
    product_id       BIGINT         NOT NULL,
    system_quantity  DECIMAL(12, 3) NOT NULL,
    counted_quantity DECIMAL(12, 3) NOT NULL,
    variance         DECIMAL(12, 3) NOT NULL,
    created_by       VARCHAR(50)    NOT NULL,
    created_date     DATETIME(6)    NOT NULL,
    updated_by       VARCHAR(50)    NULL,
    updated_date     DATETIME(6)    NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_stock_take_line_stock_take FOREIGN KEY (stock_take_id) REFERENCES stock_take (id),
    CONSTRAINT fk_stock_take_line_product FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_stock_take_line_stock_take_id ON stock_take_line (stock_take_id);
CREATE INDEX idx_stock_take_line_product_id ON stock_take_line (product_id);
