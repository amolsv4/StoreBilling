-- audit_log: append-only. No updated_* columns and no @Version - rows are never modified after
-- insert, so this table intentionally does NOT follow the BaseAuditableEntity audit-column shape.
CREATE TABLE audit_log
(
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    entity_name           VARCHAR(100) NOT NULL,
    entity_id             BIGINT       NULL,
    action                VARCHAR(30)  NOT NULL,
    performed_by_username VARCHAR(50)  NULL,
    details               TEXT         NULL,
    ip_address            VARCHAR(45)  NULL,
    created_date          DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_audit_log_action CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'LOGIN_SUCCESS', 'LOGIN_FAILURE', 'LOGOUT'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_audit_log_entity ON audit_log (entity_name, entity_id);
CREATE INDEX idx_audit_log_performed_by ON audit_log (performed_by_username);
CREATE INDEX idx_audit_log_created_date ON audit_log (created_date);
