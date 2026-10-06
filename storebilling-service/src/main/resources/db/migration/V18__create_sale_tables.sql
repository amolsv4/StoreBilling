-- Sale: POS transaction header. status has no CANCELLED value yet (Phase 6 scope per the vision
-- doc's own phase ordering - an abandoned DRAFT sale simply has no void action this phase).
-- invoice_number is nullable until COMPLETED; MySQL allows unlimited NULLs in a UNIQUE index, so
-- concurrent in-progress (DRAFT/HELD) sales for the same store never collide on the unique key.
-- sold_by_user_id is a business fact (who rang up the sale) - distinct from the technical audit
-- created_by string inherited from the BaseAuditableEntity shape. Sales are transactional records
-- with their own documented lifecycle (see SaleServiceImpl) - deliberately no is_active column,
-- unlike this app's master-data tables.
CREATE TABLE sale
(
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    organization_id  BIGINT         NOT NULL,
    store_id         BIGINT         NOT NULL,
    terminal_id      BIGINT         NOT NULL,
    customer_id      BIGINT         NULL,
    sold_by_user_id  BIGINT         NOT NULL,
    sale_date        DATETIME(6)    NOT NULL,
    status           VARCHAR(20)    NOT NULL,
    invoice_number   VARCHAR(50)    NULL,
    currency         VARCHAR(10)    NOT NULL,
    subtotal         DECIMAL(14, 2) NOT NULL DEFAULT 0,
    discount_amount  DECIMAL(14, 2) NOT NULL DEFAULT 0,
    tax_amount       DECIMAL(14, 2) NOT NULL DEFAULT 0,
    total_amount     DECIMAL(14, 2) NOT NULL DEFAULT 0,
    notes            VARCHAR(500)   NULL,
    completed_date   DATETIME(6)    NULL,
    created_by       VARCHAR(50)    NOT NULL,
    created_date     DATETIME(6)    NOT NULL,
    updated_by       VARCHAR(50)    NULL,
    updated_date     DATETIME(6)    NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_sale_store_invoice_number UNIQUE (store_id, invoice_number),
    CONSTRAINT fk_sale_organization FOREIGN KEY (organization_id) REFERENCES organization (id),
    CONSTRAINT fk_sale_store FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT fk_sale_terminal FOREIGN KEY (terminal_id) REFERENCES terminal (id),
    CONSTRAINT fk_sale_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
    CONSTRAINT fk_sale_sold_by_user FOREIGN KEY (sold_by_user_id) REFERENCES app_user (id),
    CONSTRAINT chk_sale_status CHECK (status IN ('DRAFT', 'HELD', 'COMPLETED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_sale_organization_id ON sale (organization_id);
CREATE INDEX idx_sale_store_id ON sale (store_id);
CREATE INDEX idx_sale_terminal_id ON sale (terminal_id);
CREATE INDEX idx_sale_customer_id ON sale (customer_id);
CREATE INDEX idx_sale_status ON sale (status);

-- Sale line: product_name/sku/uom and tax_group_id/tax_percentage are deliberate point-in-time
-- snapshots so a completed invoice never silently changes if the product is later renamed or a
-- tax rate is edited. quantity is DECIMAL(12,3) (not an integer) - product UOMs include
-- KG/GRAM/LITER. Deliberate, documented exception to the soft-delete convention: a line may be
-- hard DELETEd by the application while its parent sale is not yet COMPLETED (see
-- SaleServiceImpl.deleteLine) - an in-progress cart line was never a finalized record.
CREATE TABLE sale_line
(
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    sale_id          BIGINT         NOT NULL,
    product_id       BIGINT         NOT NULL,
    product_name     VARCHAR(150)   NOT NULL,
    sku              VARCHAR(50)    NOT NULL,
    uom              VARCHAR(10)    NOT NULL,
    quantity         DECIMAL(12, 3) NOT NULL,
    unit_price       DECIMAL(12, 2) NOT NULL,
    price_overridden BOOLEAN        NOT NULL DEFAULT FALSE,
    discount_amount  DECIMAL(12, 2) NOT NULL DEFAULT 0,
    tax_group_id     BIGINT         NULL,
    tax_percentage   DECIMAL(5, 2)  NOT NULL DEFAULT 0,
    tax_amount       DECIMAL(12, 2) NOT NULL DEFAULT 0,
    line_total       DECIMAL(12, 2) NOT NULL,
    created_by       VARCHAR(50)    NOT NULL,
    created_date     DATETIME(6)    NOT NULL,
    updated_by       VARCHAR(50)    NULL,
    updated_date     DATETIME(6)    NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_sale_line_sale FOREIGN KEY (sale_id) REFERENCES sale (id),
    CONSTRAINT fk_sale_line_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_sale_line_tax_group FOREIGN KEY (tax_group_id) REFERENCES tax_group (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_sale_line_sale_id ON sale_line (sale_id);
CREATE INDEX idx_sale_line_product_id ON sale_line (product_id);
