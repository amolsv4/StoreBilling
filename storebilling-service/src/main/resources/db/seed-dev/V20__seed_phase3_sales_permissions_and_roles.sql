-- Dev/local-only seed data (see application.yml: this location is only added to
-- spring.flyway.locations under the "local" profile). Phase 3 Sales/Payment permissions and role
-- assignments (plan §2). Sits between the always-applied V19 and the Inventory ledger's V21+ -
-- Flyway only requires global version uniqueness, not per-folder contiguity (V6-V8 already sit
-- between V5 and V9 today).

INSERT INTO permission (code, name, module, description, created_by, created_date, version) VALUES
 ('SALES_VIEW','View Sales','SALES','View sale records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('SALES_CREATE','Create Sales','SALES','Create sale records and lines.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('SALES_UPDATE','Update Sales','SALES','Update/hold/resume sale records and lines.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('SALES_COMPLETE','Complete Sales','SALES','Complete a sale (assign invoice number, post inventory).','SYSTEM',UTC_TIMESTAMP(6),0),
 ('SALES_PRICE_OVERRIDE','Override Sale Price','SALES','Override a sale line unit price away from the resolved price.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('PAYMENT_VIEW','View Payments','PAYMENT','View payment transaction records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('PAYMENT_CREATE','Create Payments','PAYMENT','Record payment transactions against a sale.','SYSTEM',UTC_TIMESTAMP(6),0);

-- SUPER_ADMIN + ADMIN + STORE_MANAGER: all 7, including SALES_PRICE_OVERRIDE (STORE_MANAGER is
-- supervisory, unlike Phase 2's view-only catalog scope for this role).
INSERT INTO role_permission (role_id, permission_id, created_by, created_date, version)
SELECT r.id, p.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM role r CROSS JOIN permission p
WHERE r.code IN ('SUPER_ADMIN', 'ADMIN', 'STORE_MANAGER')
  AND p.module IN ('SALES', 'PAYMENT');

-- CASHIER/SALES_USER: everything except SALES_PRICE_OVERRIDE - a cashier escalates to a manager
-- rather than self-authorizing a manual price, matching real retail policy.
INSERT INTO role_permission (role_id, permission_id, created_by, created_date, version)
SELECT r.id, p.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM role r CROSS JOIN permission p
WHERE r.code IN ('CASHIER', 'SALES_USER')
  AND p.code IN ('SALES_VIEW', 'SALES_CREATE', 'SALES_UPDATE', 'SALES_COMPLETE', 'PAYMENT_VIEW', 'PAYMENT_CREATE');

-- INVENTORY_USER/REPORT_USER: view-only.
INSERT INTO role_permission (role_id, permission_id, created_by, created_date, version)
SELECT r.id, p.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM role r CROSS JOIN permission p
WHERE r.code IN ('INVENTORY_USER', 'REPORT_USER')
  AND p.code IN ('SALES_VIEW', 'PAYMENT_VIEW');
