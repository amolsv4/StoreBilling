-- Dev/local-only seed data. Demo catalog data under the DEMOORG organization seeded by
-- V7__seed_demo_organization_store_terminal.sql: categories (with a two-level hierarchy), tax
-- components + a bundling tax group, demo products, demo customers, and a default price list
-- with product_price rows. All foreign keys are resolved via code lookups (never hardcoded ids),
-- matching V7's style.

-- ---------------------------------------------------------------------------
-- Categories (CAT-APPAREL-MEN is a child of CAT-APPAREL, exercising the hierarchy)
-- ---------------------------------------------------------------------------
INSERT INTO category (organization_id, parent_category_id, code, name, description, is_active,
                       created_by, created_date, version)
SELECT o.id, NULL, 'CAT-APPAREL', 'Apparel', 'Demo top-level apparel category.', TRUE,
       'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o
WHERE o.code = 'DEMOORG';

INSERT INTO category (organization_id, parent_category_id, code, name, description, is_active,
                       created_by, created_date, version)
SELECT o.id, NULL, 'CAT-GROCERY', 'Grocery', 'Demo top-level grocery category.', TRUE,
       'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o
WHERE o.code = 'DEMOORG';

INSERT INTO category (organization_id, parent_category_id, code, name, description, is_active,
                       created_by, created_date, version)
SELECT o.id, parent.id, 'CAT-APPAREL-MEN', 'Men''s Apparel', 'Demo child category under Apparel.', TRUE,
       'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, category parent
WHERE o.code = 'DEMOORG'
  AND parent.organization_id = o.id AND parent.code = 'CAT-APPAREL';

-- ---------------------------------------------------------------------------
-- Tax components + a bundling tax group (CGST 9% + SGST 9% = 18%)
-- ---------------------------------------------------------------------------
INSERT INTO tax_component (organization_id, code, name, tax_type, percentage, is_active,
                            created_by, created_date, version)
SELECT o.id, 'TAX-CGST-9', 'CGST 9%', 'CGST', 9.00, TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o
WHERE o.code = 'DEMOORG';

INSERT INTO tax_component (organization_id, code, name, tax_type, percentage, is_active,
                            created_by, created_date, version)
SELECT o.id, 'TAX-SGST-9', 'SGST 9%', 'SGST', 9.00, TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o
WHERE o.code = 'DEMOORG';

INSERT INTO tax_group (organization_id, code, name, description, is_active,
                        created_by, created_date, version)
SELECT o.id, 'TAXGRP-GST18', 'GST 18%', 'Demo combined GST tax group (CGST 9% + SGST 9%).', TRUE,
       'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o
WHERE o.code = 'DEMOORG';

INSERT INTO tax_group_component (tax_group_id, tax_component_id, created_by, created_date, version)
SELECT tg.id, tc.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, tax_group tg, tax_component tc
WHERE o.code = 'DEMOORG'
  AND tg.organization_id = o.id AND tg.code = 'TAXGRP-GST18'
  AND tc.organization_id = o.id AND tc.code = 'TAX-CGST-9';

INSERT INTO tax_group_component (tax_group_id, tax_component_id, created_by, created_date, version)
SELECT tg.id, tc.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, tax_group tg, tax_component tc
WHERE o.code = 'DEMOORG'
  AND tg.organization_id = o.id AND tg.code = 'TAXGRP-GST18'
  AND tc.organization_id = o.id AND tc.code = 'TAX-SGST-9';

-- ---------------------------------------------------------------------------
-- Demo products (all taxed under the GST 18% group)
-- ---------------------------------------------------------------------------
INSERT INTO product (organization_id, category_id, tax_group_id, sku, barcode, name, description,
                      brand, uom, sale_enabled, purchase_enabled, is_active,
                      created_by, created_date, version)
SELECT o.id, c.id, tg.id, 'SKU-TSHIRT-001', NULL, 'Men''s Cotton T-Shirt',
       'Demo product - men''s cotton t-shirt.', 'DemoBrand', 'PCS', TRUE, TRUE, TRUE,
       'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, category c, tax_group tg
WHERE o.code = 'DEMOORG'
  AND c.organization_id = o.id AND c.code = 'CAT-APPAREL-MEN'
  AND tg.organization_id = o.id AND tg.code = 'TAXGRP-GST18';

INSERT INTO product (organization_id, category_id, tax_group_id, sku, barcode, name, description,
                      brand, uom, sale_enabled, purchase_enabled, is_active,
                      created_by, created_date, version)
SELECT o.id, c.id, tg.id, 'SKU-JEANS-001', NULL, 'Men''s Denim Jeans',
       'Demo product - men''s denim jeans.', 'DemoBrand', 'PCS', TRUE, TRUE, TRUE,
       'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, category c, tax_group tg
WHERE o.code = 'DEMOORG'
  AND c.organization_id = o.id AND c.code = 'CAT-APPAREL-MEN'
  AND tg.organization_id = o.id AND tg.code = 'TAXGRP-GST18';

INSERT INTO product (organization_id, category_id, tax_group_id, sku, barcode, name, description,
                      brand, uom, sale_enabled, purchase_enabled, is_active,
                      created_by, created_date, version)
SELECT o.id, c.id, tg.id, 'SKU-RICE-001', NULL, 'Basmati Rice 5kg',
       'Demo product - basmati rice, 5kg pack.', 'DemoBrand', 'KG', TRUE, TRUE, TRUE,
       'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, category c, tax_group tg
WHERE o.code = 'DEMOORG'
  AND c.organization_id = o.id AND c.code = 'CAT-GROCERY'
  AND tg.organization_id = o.id AND tg.code = 'TAXGRP-GST18';

INSERT INTO product (organization_id, category_id, tax_group_id, sku, barcode, name, description,
                      brand, uom, sale_enabled, purchase_enabled, is_active,
                      created_by, created_date, version)
SELECT o.id, c.id, tg.id, 'SKU-OIL-001', NULL, 'Sunflower Oil 1L',
       'Demo product - sunflower cooking oil, 1 litre.', 'DemoBrand', 'LITER', TRUE, TRUE, TRUE,
       'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, category c, tax_group tg
WHERE o.code = 'DEMOORG'
  AND c.organization_id = o.id AND c.code = 'CAT-GROCERY'
  AND tg.organization_id = o.id AND tg.code = 'TAXGRP-GST18';

-- ---------------------------------------------------------------------------
-- Demo customers
-- ---------------------------------------------------------------------------
INSERT INTO customer (organization_id, code, name, mobile, email, address_line1, address_line2,
                       city, state, country, postal_code, tax_registration_no, credit_limit,
                       loyalty_points, is_active, created_by, created_date, version)
SELECT o.id, 'CUST-0001', 'Demo Customer One', '+10000001001', 'customer1@demoretailgroup.example',
       NULL, NULL, 'Demo City', 'Demo State', 'Demo Country', '000001', NULL, 0, 0, TRUE,
       'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o
WHERE o.code = 'DEMOORG';

INSERT INTO customer (organization_id, code, name, mobile, email, address_line1, address_line2,
                       city, state, country, postal_code, tax_registration_no, credit_limit,
                       loyalty_points, is_active, created_by, created_date, version)
SELECT o.id, 'CUST-0002', 'Demo Customer Two', '+10000001002', 'customer2@demoretailgroup.example',
       NULL, NULL, 'Demo City', 'Demo State', 'Demo Country', '000002', NULL, 0, 0, TRUE,
       'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o
WHERE o.code = 'DEMOORG';

-- ---------------------------------------------------------------------------
-- Default price list + product_price rows for the demo products
-- ---------------------------------------------------------------------------
INSERT INTO price_list (organization_id, code, name, description, effective_from, effective_to,
                         is_active, created_by, created_date, version)
SELECT o.id, 'PL-DEFAULT', 'Default Price List', 'Demo default organization-wide price list.',
       NULL, NULL, TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o
WHERE o.code = 'DEMOORG';

INSERT INTO product_price (price_list_id, product_id, base_price, selling_price, effective_from,
                            effective_to, is_active, created_by, created_date, version)
SELECT pl.id, p.id, 400.00, 499.00, NULL, NULL, TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, price_list pl, product p
WHERE o.code = 'DEMOORG'
  AND pl.organization_id = o.id AND pl.code = 'PL-DEFAULT'
  AND p.organization_id = o.id AND p.sku = 'SKU-TSHIRT-001';

INSERT INTO product_price (price_list_id, product_id, base_price, selling_price, effective_from,
                            effective_to, is_active, created_by, created_date, version)
SELECT pl.id, p.id, 1200.00, 1499.00, NULL, NULL, TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, price_list pl, product p
WHERE o.code = 'DEMOORG'
  AND pl.organization_id = o.id AND pl.code = 'PL-DEFAULT'
  AND p.organization_id = o.id AND p.sku = 'SKU-JEANS-001';

INSERT INTO product_price (price_list_id, product_id, base_price, selling_price, effective_from,
                            effective_to, is_active, created_by, created_date, version)
SELECT pl.id, p.id, 550.00, 599.00, NULL, NULL, TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, price_list pl, product p
WHERE o.code = 'DEMOORG'
  AND pl.organization_id = o.id AND pl.code = 'PL-DEFAULT'
  AND p.organization_id = o.id AND p.sku = 'SKU-RICE-001';

INSERT INTO product_price (price_list_id, product_id, base_price, selling_price, effective_from,
                            effective_to, is_active, created_by, created_date, version)
SELECT pl.id, p.id, 150.00, 179.00, NULL, NULL, TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, price_list pl, product p
WHERE o.code = 'DEMOORG'
  AND pl.organization_id = o.id AND pl.code = 'PL-DEFAULT'
  AND p.organization_id = o.id AND p.sku = 'SKU-OIL-001';
