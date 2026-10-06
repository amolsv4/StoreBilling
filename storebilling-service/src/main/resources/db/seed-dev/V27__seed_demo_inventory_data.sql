-- Dev/local-only seed data. Non-zero opening `inventory` balances + matching PURCHASE
-- `inventory_transaction` rows for the demo org/store/products already seeded by
-- V7__seed_demo_organization_store_terminal.sql and V15__seed_demo_catalog_data.sql, so local dev
-- isn't staring at all-zero stock. All foreign keys are resolved via code lookups (never
-- hardcoded ids), matching V7/V15's own style.
--
-- Store assignment mirrors each product's category: the apparel products go to the general store
-- (STORE-GEN-01), the grocery products go to the grocery store (STORE-GRC-01). reference_type is
-- 'SEED_DATA' (reference_id NULL) since no real purchase_receipt header exists for these opening
-- balances - inventory_transaction's reference_type/reference_id are deliberately open-ended and
-- carry no FK, so this is a valid, self-describing pointer.

-- ---------------------------------------------------------------------------
-- Men's Cotton T-Shirt -> General store, opening balance 100.000 PCS
-- ---------------------------------------------------------------------------
INSERT INTO inventory (product_id, store_id, quantity_on_hand, updated_by, updated_date, version)
SELECT p.id, s.id, 100.000, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, product p, store s
WHERE o.code = 'DEMOORG'
  AND p.organization_id = o.id AND p.sku = 'SKU-TSHIRT-001'
  AND s.organization_id = o.id AND s.code = 'STORE-GEN-01';

INSERT INTO inventory_transaction (product_id, store_id, transaction_type, quantity_change, balance_after,
                                    reference_type, reference_id, notes, created_by, created_date)
SELECT p.id, s.id, 'PURCHASE', 100.000, 100.000, 'SEED_DATA', NULL,
       'Opening balance seeded for local development.', 'SYSTEM', UTC_TIMESTAMP(6)
FROM organization o, product p, store s
WHERE o.code = 'DEMOORG'
  AND p.organization_id = o.id AND p.sku = 'SKU-TSHIRT-001'
  AND s.organization_id = o.id AND s.code = 'STORE-GEN-01';

-- ---------------------------------------------------------------------------
-- Men's Denim Jeans -> General store, opening balance 60.000 PCS
-- ---------------------------------------------------------------------------
INSERT INTO inventory (product_id, store_id, quantity_on_hand, updated_by, updated_date, version)
SELECT p.id, s.id, 60.000, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, product p, store s
WHERE o.code = 'DEMOORG'
  AND p.organization_id = o.id AND p.sku = 'SKU-JEANS-001'
  AND s.organization_id = o.id AND s.code = 'STORE-GEN-01';

INSERT INTO inventory_transaction (product_id, store_id, transaction_type, quantity_change, balance_after,
                                    reference_type, reference_id, notes, created_by, created_date)
SELECT p.id, s.id, 'PURCHASE', 60.000, 60.000, 'SEED_DATA', NULL,
       'Opening balance seeded for local development.', 'SYSTEM', UTC_TIMESTAMP(6)
FROM organization o, product p, store s
WHERE o.code = 'DEMOORG'
  AND p.organization_id = o.id AND p.sku = 'SKU-JEANS-001'
  AND s.organization_id = o.id AND s.code = 'STORE-GEN-01';

-- ---------------------------------------------------------------------------
-- Basmati Rice 5kg -> Grocery store, opening balance 500.000 KG
-- ---------------------------------------------------------------------------
INSERT INTO inventory (product_id, store_id, quantity_on_hand, updated_by, updated_date, version)
SELECT p.id, s.id, 500.000, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, product p, store s
WHERE o.code = 'DEMOORG'
  AND p.organization_id = o.id AND p.sku = 'SKU-RICE-001'
  AND s.organization_id = o.id AND s.code = 'STORE-GRC-01';

INSERT INTO inventory_transaction (product_id, store_id, transaction_type, quantity_change, balance_after,
                                    reference_type, reference_id, notes, created_by, created_date)
SELECT p.id, s.id, 'PURCHASE', 500.000, 500.000, 'SEED_DATA', NULL,
       'Opening balance seeded for local development.', 'SYSTEM', UTC_TIMESTAMP(6)
FROM organization o, product p, store s
WHERE o.code = 'DEMOORG'
  AND p.organization_id = o.id AND p.sku = 'SKU-RICE-001'
  AND s.organization_id = o.id AND s.code = 'STORE-GRC-01';

-- ---------------------------------------------------------------------------
-- Sunflower Oil 1L -> Grocery store, opening balance 300.000 LITER
-- ---------------------------------------------------------------------------
INSERT INTO inventory (product_id, store_id, quantity_on_hand, updated_by, updated_date, version)
SELECT p.id, s.id, 300.000, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM organization o, product p, store s
WHERE o.code = 'DEMOORG'
  AND p.organization_id = o.id AND p.sku = 'SKU-OIL-001'
  AND s.organization_id = o.id AND s.code = 'STORE-GRC-01';

INSERT INTO inventory_transaction (product_id, store_id, transaction_type, quantity_change, balance_after,
                                    reference_type, reference_id, notes, created_by, created_date)
SELECT p.id, s.id, 'PURCHASE', 300.000, 300.000, 'SEED_DATA', NULL,
       'Opening balance seeded for local development.', 'SYSTEM', UTC_TIMESTAMP(6)
FROM organization o, product p, store s
WHERE o.code = 'DEMOORG'
  AND p.organization_id = o.id AND p.sku = 'SKU-OIL-001'
  AND s.organization_id = o.id AND s.code = 'STORE-GRC-01';
