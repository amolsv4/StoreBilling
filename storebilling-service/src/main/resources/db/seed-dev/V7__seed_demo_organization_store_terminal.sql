-- Dev/local-only seed data. Demo organization -> 3 stores (one per business type) -> 1 terminal each.

INSERT INTO organization (code, name, legal_name, tax_registration_no, address_line1, address_line2,
                           city, state, country, postal_code, contact_email, contact_phone,
                           is_active, created_by, created_date, version)
VALUES ('DEMOORG', 'Demo Retail Group', 'Demo Retail Group Pvt Ltd', NULL,
        '1 Demo Plaza', NULL, 'Demo City', 'Demo State', 'Demo Country', '000000',
        'contact@demoretailgroup.example', '+10000000000', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0);

INSERT INTO store (organization_id, business_type_id, code, name, address_line1, address_line2,
                   city, state, country, postal_code, phone, email, timezone, is_active,
                   created_by, created_date, version)
SELECT o.id, bt.id, 'STORE-GOLD-01', 'Demo Gold Store #1', '10 Gold Street', NULL,
       'Demo City', 'Demo State', 'Demo Country', '000001', '+10000000001',
       'gold01@demoretailgroup.example', 'UTC', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, business_type bt
WHERE o.code = 'DEMOORG' AND bt.code = 'GOLD';

INSERT INTO store (organization_id, business_type_id, code, name, address_line1, address_line2,
                   city, state, country, postal_code, phone, email, timezone, is_active,
                   created_by, created_date, version)
SELECT o.id, bt.id, 'STORE-GRC-01', 'Demo Grocery Store #1', '20 Grocery Street', NULL,
       'Demo City', 'Demo State', 'Demo Country', '000002', '+10000000002',
       'grc01@demoretailgroup.example', 'UTC', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, business_type bt
WHERE o.code = 'DEMOORG' AND bt.code = 'GROCERY';

INSERT INTO store (organization_id, business_type_id, code, name, address_line1, address_line2,
                   city, state, country, postal_code, phone, email, timezone, is_active,
                   created_by, created_date, version)
SELECT o.id, bt.id, 'STORE-GEN-01', 'Demo General Store #1', '30 General Street', NULL,
       'Demo City', 'Demo State', 'Demo Country', '000003', '+10000000003',
       'gen01@demoretailgroup.example', 'UTC', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, business_type bt
WHERE o.code = 'DEMOORG' AND bt.code = 'GENERAL';

INSERT INTO terminal (store_id, code, name, terminal_type, device_identifier, is_active,
                       created_by, created_date, version)
SELECT s.id, 'TERM-01', CONCAT(s.name, ' - Terminal 1'), 'POS', NULL, TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM store s
WHERE s.code = 'STORE-GOLD-01';

INSERT INTO terminal (store_id, code, name, terminal_type, device_identifier, is_active,
                       created_by, created_date, version)
SELECT s.id, 'TERM-01', CONCAT(s.name, ' - Terminal 1'), 'POS', NULL, TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM store s
WHERE s.code = 'STORE-GRC-01';

INSERT INTO terminal (store_id, code, name, terminal_type, device_identifier, is_active,
                       created_by, created_date, version)
SELECT s.id, 'TERM-01', CONCAT(s.name, ' - Terminal 1'), 'POS', NULL, TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM store s
WHERE s.code = 'STORE-GEN-01';
