-- Payment transaction: full BaseAuditableEntity shape (not append-only) - kept mutable so a
-- future Phase 6 refund/reversal can transition status in place. status is COMPLETED-only this
-- phase (no reversal workflow yet). A sale has many rows (split tender).
CREATE TABLE payment_transaction
(
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    sale_id          BIGINT         NOT NULL,
    payment_method   VARCHAR(20)    NOT NULL,
    amount           DECIMAL(14, 2) NOT NULL,
    reference_number VARCHAR(100)   NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'COMPLETED',
    created_by       VARCHAR(50)    NOT NULL,
    created_date     DATETIME(6)    NOT NULL,
    updated_by       VARCHAR(50)    NULL,
    updated_date     DATETIME(6)    NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_payment_transaction_sale FOREIGN KEY (sale_id) REFERENCES sale (id),
    CONSTRAINT chk_payment_transaction_method CHECK (payment_method IN
        ('CASH', 'CARD', 'UPI', 'WALLET', 'BANK_TRANSFER', 'CREDIT', 'GIFT_CARD', 'OTHER')),
    CONSTRAINT chk_payment_transaction_status CHECK (status IN ('COMPLETED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_payment_transaction_sale_id ON payment_transaction (sale_id);

-- Notification log: append-only (same shape as audit_log - no updated_*/version columns, rows
-- are never modified after insert). "recipient" stores only the masked/truncated contact detail
-- (e.g. "jo***@example.com", "******1234") - the raw email/mobile is never written here, per the
-- PII-masking policy. Phase 3 Section 0 item 2.
CREATE TABLE notification_log
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    sale_id      BIGINT       NOT NULL,
    channel      VARCHAR(20)  NOT NULL,
    recipient    VARCHAR(100) NULL,
    status       VARCHAR(20)  NOT NULL,
    created_date DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_log_sale FOREIGN KEY (sale_id) REFERENCES sale (id),
    CONSTRAINT chk_notification_log_channel CHECK (channel IN ('EMAIL', 'SMS', 'WHATSAPP')),
    CONSTRAINT chk_notification_log_status CHECK (status IN ('SENT', 'FAILED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_notification_log_sale_id ON notification_log (sale_id);
