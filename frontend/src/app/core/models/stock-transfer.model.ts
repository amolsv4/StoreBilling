export type StockTransferStatus = 'DRAFT' | 'COMPLETED';

export interface StockTransferHeaderRequest {
  organizationId: number;
  fromStoreId: number;
  toStoreId: number;
  /** Client-supplied (max 30 chars); the backend does not generate it. */
  transferNumber: string;
  transferDate: string;
}

/** The backend's update body re-states both stores and the number; the UI keeps the stores locked, so
 *  it re-sends their current values. Update is only allowed while DRAFT. */
export interface StockTransferHeaderUpdateRequest {
  transferNumber: string;
  fromStoreId: number;
  toStoreId: number;
  transferDate: string;
}

export interface StockTransferLine {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  quantity: number;
}

export interface StockTransferLineRequest {
  productId: number;
  quantity: number;
}

/** No IN_TRANSIT state - completing a transfer atomically decrements the from-store and increments
 *  the to-store in one operation (plan §3). */
export interface StockTransferResponse {
  id: number;
  organizationId: number;
  fromStoreId: number;
  fromStoreName: string;
  toStoreId: number;
  toStoreName: string;
  transferNumber: string;
  transferDate: string;
  status: StockTransferStatus;
  lines: StockTransferLine[];
  createdDate: string;
  updatedDate: string;
}

export interface StockTransferListFilter {
  storeId?: number;
  status?: StockTransferStatus;
}
