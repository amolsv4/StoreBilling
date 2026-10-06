/**
 * Static catalog of permission codes used to populate the Role admin screen's permission
 * multi-select.
 *
 * DEVIATION NOTE: the given backend contract has no endpoint that enumerates the available
 * permission catalog (e.g. no `GET /permission`). Module names (Phase 1: ORGANIZATION, STORE,
 * TERMINAL, USER, ROLE, CONFIGURATION; Phase 2: CATEGORY, PRODUCT, CUSTOMER, TAX, PRICING - the
 * latter two each shared across two screens, Tax Component/Tax Group and Price List/Product Price
 * respectively) and the VIEW/CREATE/UPDATE actions are given explicitly in the phase spec, so this
 * list is hardcoded from that combination (no DELETE codes, matching the no-delete/soft-deactivate
 * design). If the backend later exposes a permission catalog endpoint, swap this constant for a
 * service call.
 */
export const MODULE_NAMES = [
  'ORGANIZATION',
  'STORE',
  'TERMINAL',
  'USER',
  'ROLE',
  'CONFIGURATION',
  'CATEGORY',
  'PRODUCT',
  'CUSTOMER',
  'TAX',
  'PRICING'
] as const;

export type ModuleName = (typeof MODULE_NAMES)[number];

export const PERMISSION_ACTIONS = ['VIEW', 'CREATE', 'UPDATE'] as const;

/**
 * Phase 3 (Sales/Payment/Inventory) permission codes don't fit the regular
 * `${MODULE}_${VIEW|CREATE|UPDATE}` matrix above - each module has its own irregular action set
 * (e.g. `SALES_COMPLETE`, `SALES_PRICE_OVERRIDE`, `INVENTORY_ADJUST`, `PURCHASE_RECEIVE`,
 * `TRANSFER_COMPLETE`, `STOCK_TAKE_COMPLETE`) - so they're listed explicitly here rather than
 * generated. Exact codes per the reconciled plan (§3, reconciliation note 8): `INVENTORY_*` and
 * `STOCK_TAKE_*` use these underscored forms, not the guessed `ADJUSTMENT_*`/`STOCKTAKE_*` names
 * from an earlier draft.
 */
export const PHASE3_PERMISSION_CODES: string[] = [
  'SALES_VIEW',
  'SALES_CREATE',
  'SALES_UPDATE',
  'SALES_COMPLETE',
  'SALES_PRICE_OVERRIDE',
  'PAYMENT_VIEW',
  'PAYMENT_CREATE',
  'INVENTORY_VIEW',
  'INVENTORY_ADJUST',
  'PURCHASE_VIEW',
  'PURCHASE_CREATE',
  'PURCHASE_UPDATE',
  'PURCHASE_RECEIVE',
  'TRANSFER_VIEW',
  'TRANSFER_CREATE',
  'TRANSFER_UPDATE',
  'TRANSFER_COMPLETE',
  'STOCK_TAKE_VIEW',
  'STOCK_TAKE_CREATE',
  'STOCK_TAKE_UPDATE',
  'STOCK_TAKE_COMPLETE'
];

export const PERMISSION_CODES: string[] = [
  ...MODULE_NAMES.flatMap((module) => PERMISSION_ACTIONS.map((action) => `${module}_${action}`)),
  ...PHASE3_PERMISSION_CODES
];
