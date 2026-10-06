-- Store: belongs to exactly one organization and one business type.
CREATE TABLE store
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    organization_id  BIGINT       NOT NULL,
    business_type_id BIGINT       NOT NULL,
    code             VARCHAR(30)  NOT NULL,
    name             VARCHAR(150) NOT NULL,
    address_line1    VARCHAR(150) NULL,
    address_line2    VARCHAR(150) NULL,
    city             VARCHAR(100) NULL,
    state            VARCHAR(100) NULL,
    country          VARCHAR(100) NULL,
    postal_code      VARCHAR(20)  NULL,
    phone            VARCHAR(30)  NULL,
    email            VARCHAR(150) NULL,
    timezone         VARCHAR(50)  NOT NULL DEFAULT 'UTC',
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by       VARCHAR(50)  NOT NULL,
    created_date     DATETIME(6)  NOT NULL,
    updated_by       VARCHAR(50)  NULL,
    updated_date     DATETIME(6)  NULL,
    version          BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_store_code UNIQUE (code),
    CONSTRAINT fk_store_organization FOREIGN KEY (organization_id) REFERENCES organization (id),
    CONSTRAINT fk_store_business_type FOREIGN KEY (business_type_id) REFERENCES business_type (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_store_organization_id ON store (organization_id);
CREATE INDEX idx_store_business_type_id ON store (business_type_id);

-- Terminal: belongs to exactly one store. Business type is inherited transitively via store,
-- so terminal intentionally has no business_type_id column.
CREATE TABLE terminal
(
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    store_id          BIGINT      NOT NULL,
    code              VARCHAR(30) NOT NULL,
    name              VARCHAR(150) NOT NULL,
    terminal_type     VARCHAR(20) NOT NULL,
    device_identifier VARCHAR(100) NULL,
    is_active         BOOLEAN     NOT NULL DEFAULT TRUE,
    created_by        VARCHAR(50) NOT NULL,
    created_date      DATETIME(6) NOT NULL,
    updated_by        VARCHAR(50) NULL,
    updated_date      DATETIME(6) NULL,
    version           BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_terminal_store_code UNIQUE (store_id, code),
    CONSTRAINT fk_terminal_store FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT chk_terminal_type CHECK (terminal_type IN ('POS', 'MPOS', 'KIOSK'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_terminal_store_id ON terminal (store_id);
