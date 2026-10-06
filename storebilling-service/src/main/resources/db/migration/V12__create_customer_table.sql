-- Customer: retail customer, scoped to exactly one organization.
CREATE TABLE customer
(
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    organization_id     BIGINT        NOT NULL,
    code                VARCHAR(30)   NOT NULL,
    name                VARCHAR(150)  NOT NULL,
    mobile              VARCHAR(20)   NOT NULL,
    email               VARCHAR(150)  NULL,
    address_line1       VARCHAR(150)  NULL,
    address_line2       VARCHAR(150)  NULL,
    city                VARCHAR(100)  NULL,
    state               VARCHAR(100)  NULL,
    country             VARCHAR(100)  NULL,
    postal_code         VARCHAR(20)   NULL,
    tax_registration_no VARCHAR(50)   NULL,
    credit_limit        DECIMAL(12, 2) NOT NULL DEFAULT 0,
    loyalty_points      INT           NOT NULL DEFAULT 0,
    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_by          VARCHAR(50)   NOT NULL,
    created_date        DATETIME(6)   NOT NULL,
    updated_by          VARCHAR(50)   NULL,
    updated_date        DATETIME(6)   NULL,
    version             BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_customer_organization_code UNIQUE (organization_id, code),
    CONSTRAINT fk_customer_organization FOREIGN KEY (organization_id) REFERENCES organization (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_customer_organization_id ON customer (organization_id);
