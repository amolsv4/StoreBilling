-- Dev/local-only seed data (see application.yml: this location is only added to
-- spring.flyway.locations under the "local" profile). Never applied in a bare/prod run.

-- ---------------------------------------------------------------------------
-- Business types (reference rows only - no product/catalog schema in Phase 1)
-- ---------------------------------------------------------------------------
INSERT INTO business_type (code, name, description, is_active, created_by, created_date, version)
VALUES ('GOLD', 'Gold / Jewellery', 'Gold and jewellery retail business type reference row.', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('GROCERY', 'Grocery', 'Grocery retail business type reference row.', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('GENERAL', 'General Retail', 'General retail business type reference row.', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0);

-- ---------------------------------------------------------------------------
-- Roles
-- ---------------------------------------------------------------------------
INSERT INTO role (code, name, description, is_system_role, created_by, created_date, version)
VALUES ('SUPER_ADMIN', 'Super Administrator', 'Full platform access across all organizations.', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('ADMIN', 'Administrator', 'Full administrative access within an organization.', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('STORE_MANAGER', 'Store Manager', 'Manages stores/terminals/users within their scope.', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('CASHIER', 'Cashier', 'Front-of-store terminal operator (baseline view access).', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('SALES_USER', 'Sales User', 'Sales-floor user (baseline view access).', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('INVENTORY_USER', 'Inventory User', 'Inventory-focused user (baseline view access).', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('REPORT_USER', 'Report User', 'Read-only reporting user (baseline view access).', TRUE, 'SYSTEM', UTC_TIMESTAMP(6), 0);

-- ---------------------------------------------------------------------------
-- Permissions, grouped by module
-- ---------------------------------------------------------------------------
INSERT INTO permission (code, name, module, description, created_by, created_date, version)
VALUES ('ORGANIZATION_VIEW', 'View Organization', 'ORGANIZATION', 'View organization records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('ORGANIZATION_CREATE', 'Create Organization', 'ORGANIZATION', 'Create organization records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('ORGANIZATION_UPDATE', 'Update Organization', 'ORGANIZATION', 'Update organization records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),

       ('STORE_VIEW', 'View Store', 'STORE', 'View store records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('STORE_CREATE', 'Create Store', 'STORE', 'Create store records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('STORE_UPDATE', 'Update Store', 'STORE', 'Update store records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),

       ('TERMINAL_VIEW', 'View Terminal', 'TERMINAL', 'View terminal records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('TERMINAL_CREATE', 'Create Terminal', 'TERMINAL', 'Create terminal records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('TERMINAL_UPDATE', 'Update Terminal', 'TERMINAL', 'Update terminal records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),

       ('USER_VIEW', 'View User', 'USER', 'View user records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('USER_CREATE', 'Create User', 'USER', 'Create user records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('USER_UPDATE', 'Update User', 'USER', 'Update user records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),

       ('ROLE_VIEW', 'View Role', 'ROLE', 'View role records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('ROLE_CREATE', 'Create Role', 'ROLE', 'Create role records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('ROLE_UPDATE', 'Update Role', 'ROLE', 'Update role records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),

       ('CONFIGURATION_VIEW', 'View Configuration', 'CONFIGURATION', 'View configuration records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('CONFIGURATION_CREATE', 'Create Configuration', 'CONFIGURATION', 'Create configuration records.', 'SYSTEM', UTC_TIMESTAMP(6), 0),
       ('CONFIGURATION_UPDATE', 'Update Configuration', 'CONFIGURATION', 'Update configuration records.', 'SYSTEM', UTC_TIMESTAMP(6), 0);

-- ---------------------------------------------------------------------------
-- Role -> Permission mappings
-- ---------------------------------------------------------------------------

-- SUPER_ADMIN: every permission
INSERT INTO role_permission (role_id, permission_id, created_by, created_date, version)
SELECT r.id, p.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM role r
         CROSS JOIN permission p
WHERE r.code = 'SUPER_ADMIN';

-- ADMIN: every permission (full administrative access within an organization)
INSERT INTO role_permission (role_id, permission_id, created_by, created_date, version)
SELECT r.id, p.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM role r
         CROSS JOIN permission p
WHERE r.code = 'ADMIN';

-- STORE_MANAGER: store/terminal/user view + update within their scope
INSERT INTO role_permission (role_id, permission_id, created_by, created_date, version)
SELECT r.id, p.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM role r
         CROSS JOIN permission p
WHERE r.code = 'STORE_MANAGER'
  AND p.code IN ('STORE_VIEW', 'STORE_UPDATE',
                 'TERMINAL_VIEW', 'TERMINAL_UPDATE',
                 'USER_VIEW', 'USER_UPDATE',
                 'CONFIGURATION_VIEW');

-- CASHIER / SALES_USER / INVENTORY_USER / REPORT_USER: baseline VIEW-only permissions
-- (their real functional modules - POS sales, inventory, reporting - are out of Phase 1 scope)
INSERT INTO role_permission (role_id, permission_id, created_by, created_date, version)
SELECT r.id, p.id, 'SYSTEM', UTC_TIMESTAMP(6), 0
FROM role r
         CROSS JOIN permission p
WHERE r.code IN ('CASHIER', 'SALES_USER', 'INVENTORY_USER', 'REPORT_USER')
  AND p.code IN ('STORE_VIEW', 'TERMINAL_VIEW', 'CONFIGURATION_VIEW');
