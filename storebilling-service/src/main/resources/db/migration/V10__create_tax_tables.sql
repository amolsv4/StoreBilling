-- Tax component: a single named tax rate (e.g. CGST 9%), scoped to exactly one organization.
CREATE TABLE tax_component
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    organization_id BIGINT    NOT NULL,
    code         VARCHAR(30)  NOT NULL,
    name         VARCHAR(150) NOT NULL,
    tax_type     VARCHAR(20)  NOT NULL,
    percentage   DECIMAL(5, 2) NOT NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by   VARCHAR(50)  NOT NULL,
    created_date DATETIME(6)  NOT NULL,
    updated_by   VARCHAR(50)  NULL,
    updated_date DATETIME(6)  NULL,
    version      BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_tax_component_organization_code UNIQUE (organization_id, code),
    CONSTRAINT fk_tax_component_organization FOREIGN KEY (organization_id) REFERENCES organization (id),
    CONSTRAINT chk_tax_component_tax_type CHECK (tax_type IN ('CGST', 'SGST', 'IGST', 'CESS', 'GENERAL'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_tax_component_organization_id ON tax_component (organization_id);

-- Tax group: named bundle of one or more tax components (via tax_group_component). The combined
-- percentage is never stored here - it is computed on demand in the service layer.
CREATE TABLE tax_group
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    organization_id BIGINT       NOT NULL,
    code            VARCHAR(30)  NOT NULL,
    name            VARCHAR(150) NOT NULL,
    description     VARCHAR(500) NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(50)  NOT NULL,
    created_date    DATETIME(6)  NOT NULL,
    updated_by      VARCHAR(50)  NULL,
    updated_date    DATETIME(6)  NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_tax_group_organization_code UNIQUE (organization_id, code),
    CONSTRAINT fk_tax_group_organization FOREIGN KEY (organization_id) REFERENCES organization (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_tax_group_organization_id ON tax_group (organization_id);

-- Tax group <-> tax component join table. Pure association table (like role_permission) - no
-- organization_id/code of its own.
CREATE TABLE tax_group_component
(
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    tax_group_id     BIGINT      NOT NULL,
    tax_component_id BIGINT      NOT NULL,
    created_by       VARCHAR(50) NOT NULL,
    created_date     DATETIME(6) NOT NULL,
    updated_by       VARCHAR(50) NULL,
    updated_date     DATETIME(6) NULL,
    version          BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_tax_group_component UNIQUE (tax_group_id, tax_component_id),
    CONSTRAINT fk_tax_group_component_tax_group FOREIGN KEY (tax_group_id) REFERENCES tax_group (id),
    CONSTRAINT fk_tax_group_component_tax_component FOREIGN KEY (tax_component_id) REFERENCES tax_component (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
