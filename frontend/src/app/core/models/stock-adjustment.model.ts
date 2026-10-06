export type StockAdjustmentType = 'ADJUSTMENT' | 'DAMAGE' | 'EXPIRY';

export const STOCK_ADJUSTMENT_TYPES: StockAdjustmentType[] = ['ADJUSTMENT', 'DAMAGE', 'EXPIRY'];

/** Single flat create-only record (no header+lines, no update) - a correction is one
 *  product/store/reason event. The service rejects a positive `quantityChange` for DAMAGE/EXPIRY. */
export interface StockAdjustmentCreateRequest {
  storeId: number;
  productId: number;
  adjustmentType: StockAdjustmentType;
  quantityChange: number;
  reason: string;
}

export interface StockAdjustment {
  id: number;
  storeId: number;
  storeName: string;
  productId: number;
  productName: string;
  productSku: string;
  adjustmentType: StockAdjustmentType;
  quantityChange: number;
  reason: string;
  createdDate: string;
}
