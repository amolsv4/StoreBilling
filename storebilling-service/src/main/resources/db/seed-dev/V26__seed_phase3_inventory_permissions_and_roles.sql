-- Dev/local-only seed data (see application.yml: this location is only added to
-- spring.flyway.locations under the "local" profile). Phase 3 Inventory ledger permissions and
-- role assignments (plan §3) - 14 codes across 4 modules (INVENTORY/PURCHASE/TRANSFER/STOCK_TAKE),
-- exact filtered-CROSS-JOIN style as V14/V20.

INSERT INTO permission (code, name, module, description, created_by, created_date, version) VALUES
 ('INVENTORY_VIEW','View Inventory','INVENTORY','View current stock balances and transaction history.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('INVENTORY_ADJUST','Adjust Inventory','INVENTORY','Create stock adjustment/damage/expiry entries.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('PURCHASE_VIEW','View Purchase Receipt','PURCHASE','View purchase receipt records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('PURCHASE_CREATE','Create Purchase Receipt','PURCHASE','Create purchase receipt records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('PURCHASE_UPDATE','Update Purchase Receipt','PURCHASE','Update purchase receipt records and lines while draft.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('PURCHASE_RECEIVE','Receive Purchase Receipt','PURCHASE','Post a purchase receipt to the inventory ledger.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('TRANSFER_VIEW','View Stock Transfer','TRANSFER','View stock transfer records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('TRANSFER_CREATE','Create Stock Transfer','TRANSFER','Create stock transfer records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('TRANSFER_UPDATE','Update Stock Transfer','TRANSFER','Update stock transfer records and lines while draft.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('TRANSFER_COMPLETE','Complete Stock Transfer','TRANSFER','Post a stock transfer to the inventory ledger.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('STOCK_TAKE_VIEW','View Stock Take','STOCK_TAKE','View stock take records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('STOCK_TAKE_CREATE','Create Stock Take','STOCK_TAKE','Create stock take records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('STOCK_TAKE_UPDATE','Update Stock Take','STOCK_TAKE','Update stock take records and lines while draft.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('STOCK_TAKE_COMPLETE','Complete Stock Take','STOCK_TAKE','Post a stock take reconciliation to the inventory ledger.','SYSTEM',UTC_TIMESTAMP(6),0);

-- SUPER_ADMIN/ADMIN and STORE_MANAGER/INVENTORY_USER: all 14 - STORE_MANAGER and INVENTORY_USER
-- both get the full day-to-day operational set (unlike Phase 2's centrally-managed catalog data
-- where STORE_MANAGER was view-only); scope is enforced by store-access rules, not by withholding
-- the permission.
INSERT INTO role_permission (role_id, permission_id, created_by, created_date, version)
SELECT r.id, p.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM role r CROSS JOIN permission p
WHERE r.code IN ('SUPER_ADMIN', 'ADMIN', 'STORE_MANAGER', 'INVENTORY_USER')
  AND p.module IN ('INVENTORY', 'PURCHASE', 'TRANSFER', 'STOCK_TAKE');

-- CASHIER/SALES_USER: INVENTORY_VIEW only (a cashier can see stock levels, nothing else here).
INSERT INTO role_permission (role_id, permission_id, created_by, created_date, version)
SELECT r.id, p.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM role r CROSS JOIN permission p
WHERE r.code IN ('CASHIER', 'SALES_USER')
  AND p.code = 'INVENTORY_VIEW';

-- REPORT_USER: VIEW across all four modules.
INSERT INTO role_permission (role_id, permission_id, created_by, created_date, version)
SELECT r.id, p.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM role r CROSS JOIN permission p
WHERE r.code = 'REPORT_USER'
  AND p.code IN ('INVENTORY_VIEW', 'PURCHASE_VIEW', 'TRANSFER_VIEW', 'STOCK_TAKE_VIEW');
