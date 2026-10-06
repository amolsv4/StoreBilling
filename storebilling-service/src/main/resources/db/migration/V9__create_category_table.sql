-- Category: product category, scoped to exactly one organization. Self-referencing
-- parent_category_id supports a hierarchy (top-level categories have parent_category_id = NULL).
-- Cycle prevention is enforced in the service layer, not by this schema.
CREATE TABLE category
(
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    organization_id    BIGINT       NOT NULL,
    parent_category_id BIGINT       NULL,
    code               VARCHAR(30)  NOT NULL,
    name               VARCHAR(150) NOT NULL,
    description        VARCHAR(500) NULL,
    is_active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by         VARCHAR(50)  NOT NULL,
    created_date       DATETIME(6)  NOT NULL,
    updated_by         VARCHAR(50)  NULL,
    updated_date       DATETIME(6)  NULL,
    version            BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_category_organization_code UNIQUE (organization_id, code),
    CONSTRAINT fk_category_organization FOREIGN KEY (organization_id) REFERENCES organization (id),
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id) REFERENCES category (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_category_organization_id ON category (organization_id);
CREATE INDEX idx_category_parent_category_id ON category (parent_category_id);
