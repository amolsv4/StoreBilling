/** Matches the backend's `inventory_transaction.transaction_type` CHECK constraint (plan §3). */
export type InventoryTransactionType =
  | 'PURCHASE'
  | 'PURCHASE_RETURN'
  | 'SALE'
  | 'SALE_RETURN'
  | 'TRANSFER_IN'
  | 'TRANSFER_OUT'
  | 'ADJUSTMENT'
  | 'DAMAGE'
  | 'EXPIRY'
  | 'STOCK_TAKE';

export const INVENTORY_TRANSACTION_TYPES: InventoryTransactionType[] = [
  'PURCHASE',
  'PURCHASE_RETURN',
  'SALE',
  'SALE_RETURN',
  'TRANSFER_IN',
  'TRANSFER_OUT',
  'ADJUSTMENT',
  'DAMAGE',
  'EXPIRY',
  'STOCK_TAKE'
];

/** A current stock balance row for one (product, store) pair. */
export interface InventoryBalance {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  storeId: number;
  storeName: string;
  quantityOnHand: number;
  updatedDate: string;
}

/** Append-only ledger row; `referenceType`/`referenceId` are deliberately open-ended (no FK) and
 *  point at different tables depending on `transactionType` (sale, purchase receipt, etc.). */
export interface InventoryTransaction {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  storeId: number;
  storeName: string;
  transactionType: InventoryTransactionType;
  quantityChange: number;
  balanceAfter: number;
  referenceType: string | null;
  referenceId: number | null;
  notes: string | null;
  createdDate: string;
}

export interface InventoryBalanceFilter {
  storeId?: number;
  productId?: number;
}

export interface InventoryTransactionFilter {
  productId?: number;
  storeId?: number;
  type?: InventoryTransactionType;
}
