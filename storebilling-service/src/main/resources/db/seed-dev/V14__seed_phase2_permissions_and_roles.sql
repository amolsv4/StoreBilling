-- Dev/local-only seed data (see application.yml: this location is only added to
-- spring.flyway.locations under the "local" profile). Phase 2 permissions for the category, tax,
-- product, customer and pricing modules, plus their role assignments.

INSERT INTO permission (code, name, module, description, created_by, created_date, version) VALUES
 ('CATEGORY_VIEW','View Category','CATEGORY','View category records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('CATEGORY_CREATE','Create Category','CATEGORY','Create category records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('CATEGORY_UPDATE','Update Category','CATEGORY','Update category records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('PRODUCT_VIEW','View Product','PRODUCT','View product records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('PRODUCT_CREATE','Create Product','PRODUCT','Create product records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('PRODUCT_UPDATE','Update Product','PRODUCT','Update product records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('CUSTOMER_VIEW','View Customer','CUSTOMER','View customer records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('CUSTOMER_CREATE','Create Customer','CUSTOMER','Create customer records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('CUSTOMER_UPDATE','Update Customer','CUSTOMER','Update customer records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('TAX_VIEW','View Tax','TAX','View tax component/group records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('TAX_CREATE','Create Tax','TAX','Create tax component/group records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('TAX_UPDATE','Update Tax','TAX','Update tax component/group records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('PRICING_VIEW','View Pricing','PRICING','View price list/product price records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('PRICING_CREATE','Create Pricing','PRICING','Create price list/product price records.','SYSTEM',UTC_TIMESTAMP(6),0),
 ('PRICING_UPDATE','Update Pricing','PRICING','Update price list/product price records.','SYSTEM',UTC_TIMESTAMP(6),0);

-- SUPER_ADMIN + ADMIN: filtered to just the 15 new rows (a bare CROSS JOIN would re-insert the 18
-- existing Phase 1 permissions and violate uk_role_permission)
INSERT INTO role_permission (role_id, permission_id, created_by, created_date, version)
SELECT r.id, p.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM role r CROSS JOIN permission p
WHERE r.code IN ('SUPER_ADMIN', 'ADMIN')
  AND p.module IN ('CATEGORY', 'PRODUCT', 'CUSTOMER', 'TAX', 'PRICING');

INSERT INTO role_permission (role_id, permission_id, created_by, created_date, version)
SELECT r.id, p.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM role r CROSS JOIN permission p
WHERE r.code = 'STORE_MANAGER'
  AND p.code IN ('CATEGORY_VIEW', 'PRODUCT_VIEW', 'CUSTOMER_VIEW', 'TAX_VIEW', 'PRICING_VIEW');

INSERT INTO role_permission (role_id, permission_id, created_by, created_date, version)
SELECT r.id, p.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM role r CROSS JOIN permission p
WHERE r.code IN ('CASHIER', 'SALES_USER', 'INVENTORY_USER', 'REPORT_USER')
  AND p.code IN ('CATEGORY_VIEW', 'PRODUCT_VIEW', 'CUSTOMER_VIEW', 'TAX_VIEW', 'PRICING_VIEW');
