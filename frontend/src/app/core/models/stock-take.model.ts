export type StockTakeStatus = 'DRAFT' | 'COMPLETED';

export interface StockTakeHeaderRequest {
  organizationId: number;
  storeId: number;
  /** Client-supplied (max 30 chars); the backend does not generate it. */
  stockTakeNumber: string;
  stockTakeDate: string;
}

/** `organizationId`/`storeId` are immutable after creation; update is only allowed while DRAFT. */
export interface StockTakeHeaderUpdateRequest {
  stockTakeNumber: string;
  stockTakeDate: string;
}

/** `systemQuantity` is a server snapshot taken at line-add time; `variance` is only meaningful
 *  (non-null) after `complete()` re-diffs against the *live* balance (plan §3, stock-take completion
 *  algorithm) - it is not simply `countedQuantity - systemQuantity` client-side. */
export interface StockTakeLine {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  systemQuantity: number;
  countedQuantity: number | null;
  variance: number | null;
}

/** The backend requires the counted quantity up front (>= 0); `systemQuantity` is snapshotted server-side. */
export interface StockTakeLineAddRequest {
  productId: number;
  countedQuantity: number;
}

export interface StockTakeLineUpdateRequest {
  countedQuantity: number;
}

export interface StockTakeResponse {
  id: number;
  organizationId: number;
  storeId: number;
  storeCode: string;
  storeName: string;
  stockTakeNumber: string;
  stockTakeDate: string;
  status: StockTakeStatus;
  lines: StockTakeLine[];
  createdDate: string;
  updatedDate: string;
}

export interface StockTakeListFilter {
  storeId?: number;
  status?: StockTakeStatus;
}
