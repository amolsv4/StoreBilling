-- Price list: named price list, scoped to exactly one organization. Holds product_price rows.
CREATE TABLE price_list
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    organization_id BIGINT       NOT NULL,
    code            VARCHAR(30)  NOT NULL,
    name            VARCHAR(150) NOT NULL,
    description     VARCHAR(500) NULL,
    effective_from  DATE         NULL,
    effective_to    DATE         NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(50)  NOT NULL,
    created_date    DATETIME(6)  NOT NULL,
    updated_by      VARCHAR(50)  NULL,
    updated_date    DATETIME(6)  NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_price_list_organization_code UNIQUE (organization_id, code),
    CONSTRAINT fk_price_list_organization FOREIGN KEY (organization_id) REFERENCES organization (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_price_list_organization_id ON price_list (organization_id);

-- Product price: a product's price within a price list. No organization_id column of its own -
-- organization is inherited transitively via product_price -> product -> product.organization_id.
CREATE TABLE product_price
(
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    price_list_id  BIGINT        NOT NULL,
    product_id     BIGINT        NOT NULL,
    base_price     DECIMAL(12, 2) NOT NULL,
    selling_price  DECIMAL(12, 2) NOT NULL,
    effective_from DATE          NULL,
    effective_to   DATE          NULL,
    is_active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_by     VARCHAR(50)   NOT NULL,
    created_date   DATETIME(6)   NOT NULL,
    updated_by     VARCHAR(50)   NULL,
    updated_date   DATETIME(6)   NULL,
    version        BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_product_price UNIQUE (price_list_id, product_id),
    CONSTRAINT fk_product_price_price_list FOREIGN KEY (price_list_id) REFERENCES price_list (id),
    CONSTRAINT fk_product_price_product FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_product_price_price_list_id ON product_price (price_list_id);
CREATE INDEX idx_product_price_product_id ON product_price (product_id);
