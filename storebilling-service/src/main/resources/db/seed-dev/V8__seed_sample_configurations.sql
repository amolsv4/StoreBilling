-- Dev/local-only seed data proving the full TERMINAL -> STORE -> ORGANIZATION -> BUSINESS_TYPE ->
-- GLOBAL -> DEFAULT resolution chain described in the configuration engine design.
--
-- GLOBAL scope uses the sentinel scope_id = 0.

-- GLOBAL: default currency is USD everywhere unless overridden.
INSERT INTO app_configuration (config_key, config_value, data_type, scope_type, scope_id, description,
                                is_active, created_by, created_date, version)
VALUES ('DEFAULT_CURRENCY', 'USD', 'STRING', 'GLOBAL', 0, 'Default currency applied platform-wide.',
        TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0);

-- BUSINESS_TYPE = GOLD: show gold rate ticker.
INSERT INTO app_configuration (config_key, config_value, data_type, scope_type, scope_id, description,
                                is_active, created_by, created_date, version)
SELECT 'SHOW_GOLD_RATE', 'true', 'BOOLEAN', 'BUSINESS_TYPE', bt.id,
       'Whether the live gold rate ticker is shown.', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM business_type bt
WHERE bt.code = 'GOLD';

-- BUSINESS_TYPE = GROCERY: show batch/lot tracking fields.
INSERT INTO app_configuration (config_key, config_value, data_type, scope_type, scope_id, description,
                                is_active, created_by, created_date, version)
SELECT 'SHOW_BATCH', 'true', 'BOOLEAN', 'BUSINESS_TYPE', bt.id,
       'Whether batch/lot tracking fields are shown.', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM business_type bt
WHERE bt.code = 'GROCERY';

-- BUSINESS_TYPE = GENERAL: neither gold rate nor batch tracking applies.
INSERT INTO app_configuration (config_key, config_value, data_type, scope_type, scope_id, description,
                                is_active, created_by, created_date, version)
SELECT 'SHOW_GOLD_RATE', 'false', 'BOOLEAN', 'BUSINESS_TYPE', bt.id,
       'Whether the live gold rate ticker is shown.', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM business_type bt
WHERE bt.code = 'GENERAL';

INSERT INTO app_configuration (config_key, config_value, data_type, scope_type, scope_id, description,
                                is_active, created_by, created_date, version)
SELECT 'SHOW_BATCH', 'false', 'BOOLEAN', 'BUSINESS_TYPE', bt.id,
       'Whether batch/lot tracking fields are shown.', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM business_type bt
WHERE bt.code = 'GENERAL';

-- STORE override on STORE-GOLD-01: local currency is INR instead of the GLOBAL default USD.
INSERT INTO app_configuration (config_key, config_value, data_type, scope_type, scope_id, description,
                                is_active, created_by, created_date, version)
SELECT 'DEFAULT_CURRENCY', 'INR', 'STRING', 'STORE', s.id,
       'Store-level currency override.', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM store s
WHERE s.code = 'STORE-GOLD-01';

-- TERMINAL override on STORE-GOLD-01's TERM-01: this specific terminal hides the gold rate,
-- overriding the BUSINESS_TYPE=GOLD default of true.
INSERT INTO app_configuration (config_key, config_value, data_type, scope_type, scope_id, description,
                                is_active, created_by, created_date, version)
SELECT 'SHOW_GOLD_RATE', 'false', 'BOOLEAN', 'TERMINAL', t.id,
       'Terminal-level override hiding the gold rate ticker on this specific terminal.',
       TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM terminal t
         JOIN store s ON t.store_id = s.id
WHERE s.code = 'STORE-GOLD-01'
  AND t.code = 'TERM-01';
