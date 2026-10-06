-- Organization: the top-level tenant/owner entity. Stores belong to exactly one organization.
CREATE TABLE organization
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    code                VARCHAR(30)  NOT NULL,
    name                VARCHAR(150) NOT NULL,
    legal_name          VARCHAR(200) NULL,
    tax_registration_no VARCHAR(50)  NULL,
    address_line1       VARCHAR(150) NULL,
    address_line2       VARCHAR(150) NULL,
    city                VARCHAR(100) NULL,
    state               VARCHAR(100) NULL,
    country             VARCHAR(100) NULL,
    postal_code         VARCHAR(20)  NULL,
    contact_email       VARCHAR(150) NULL,
    contact_phone       VARCHAR(30)  NULL,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by          VARCHAR(50)  NOT NULL,
    created_date        DATETIME(6)  NOT NULL,
    updated_by          VARCHAR(50)  NULL,
    updated_date        DATETIME(6)  NULL,
    version             BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_organization_code UNIQUE (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Business type: independent global reference lookup (GOLD / GROCERY / GENERAL).
-- Not organization-owned; a store references one business_type row.
CREATE TABLE business_type
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    code         VARCHAR(30)  NOT NULL,
    name         VARCHAR(100) NOT NULL,
    description  VARCHAR(500) NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by   VARCHAR(50)  NOT NULL,
    created_date DATETIME(6)  NOT NULL,
    updated_by   VARCHAR(50)  NULL,
    updated_date DATETIME(6)  NULL,
    version      BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_business_type_code UNIQUE (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
