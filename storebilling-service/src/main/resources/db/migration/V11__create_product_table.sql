-- Product: sellable/purchasable catalog item, scoped to exactly one organization. Belongs to
-- exactly one category and, optionally, one tax group.
CREATE TABLE product
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    organization_id   BIGINT       NOT NULL,
    category_id       BIGINT       NOT NULL,
    tax_group_id      BIGINT       NULL,
    sku               VARCHAR(50)  NOT NULL,
    barcode           VARCHAR(50)  NULL,
    name              VARCHAR(150) NOT NULL,
    description       VARCHAR(500) NULL,
    brand             VARCHAR(100) NULL,
    uom               VARCHAR(10)  NOT NULL,
    sale_enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    purchase_enabled  BOOLEAN      NOT NULL DEFAULT TRUE,
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by        VARCHAR(50)  NOT NULL,
    created_date      DATETIME(6)  NOT NULL,
    updated_by        VARCHAR(50)  NULL,
    updated_date      DATETIME(6)  NULL,
    version           BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_product_organization_sku UNIQUE (organization_id, sku),
    CONSTRAINT uk_product_organization_barcode UNIQUE (organization_id, barcode),
    CONSTRAINT fk_product_organization FOREIGN KEY (organization_id) REFERENCES organization (id),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_product_tax_group FOREIGN KEY (tax_group_id) REFERENCES tax_group (id),
    CONSTRAINT chk_product_uom CHECK (uom IN ('PCS', 'KG', 'GRAM', 'LITER', 'BOX', 'PACK'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_product_organization_id ON product (organization_id);
CREATE INDEX idx_product_category_id ON product (category_id);
CREATE INDEX idx_product_tax_group_id ON product (tax_group_id);
