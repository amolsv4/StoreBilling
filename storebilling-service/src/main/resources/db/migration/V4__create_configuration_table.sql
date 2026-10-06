-- app_configuration: multi-scope configuration engine storage.
-- scope_id is polymorphic (its meaning depends on scope_type) and intentionally has NO database
-- foreign key - it is validated in the service layer against the correct table for the given
-- scope_type. GLOBAL scope uses the sentinel scope_id = 0 (a real, non-null value is required so
-- the unique index behaves predictably across all scope types).
CREATE TABLE app_configuration
(
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    config_key   VARCHAR(100)  NOT NULL,
    config_value VARCHAR(1000) NOT NULL,
    data_type    VARCHAR(20)   NOT NULL,
    scope_type   VARCHAR(20)   NOT NULL,
    scope_id     BIGINT        NOT NULL,
    description  VARCHAR(500)  NULL,
    is_active    BOOLEAN       NOT NULL DEFAULT TRUE,
    created_by   VARCHAR(50)   NOT NULL,
    created_date DATETIME(6)   NOT NULL,
    updated_by   VARCHAR(50)   NULL,
    updated_date DATETIME(6)   NULL,
    version      BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_app_configuration_key_scope UNIQUE (config_key, scope_type, scope_id),
    CONSTRAINT chk_app_configuration_data_type CHECK (data_type IN ('STRING', 'BOOLEAN', 'NUMBER', 'JSON')),
    CONSTRAINT chk_app_configuration_scope_type CHECK (scope_type IN ('GLOBAL', 'BUSINESS_TYPE', 'ORGANIZATION', 'STORE', 'TERMINAL'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_app_configuration_key ON app_configuration (config_key);
CREATE INDEX idx_app_configuration_scope ON app_configuration (scope_type, scope_id);
