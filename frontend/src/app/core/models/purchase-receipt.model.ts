export type PurchaseReceiptStatus = 'DRAFT' | 'RECEIVED';

export interface PurchaseReceiptHeaderRequest {
  organizationId: number;
  storeId: number;
  /** Free text - no structured Supplier master exists yet (plan §3, "Supplier decision"). */
  supplierName: string;
  /** Client-supplied (max 30 chars); the backend does not generate it. */
  receiptNumber: string;
  receiptDate: string;
}

/** `organizationId`/`storeId` are immutable after creation; update is only allowed while DRAFT. */
export interface PurchaseReceiptHeaderUpdateRequest {
  supplierName: string;
  receiptNumber: string;
  receiptDate: string;
}

export interface PurchaseReceiptLine {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  quantity: number;
  unitCost: number;
  lineTotal: number;
}

export interface PurchaseReceiptLineRequest {
  productId: number;
  quantity: number;
  unitCost: number;
}

/** Every mutating endpoint (header update, line add/update/remove, receive) returns this full
 *  header+lines shape, mirroring Sales' response convention (plan §5/§6 - incremental model). */
export interface PurchaseReceiptResponse {
  id: number;
  organizationId: number;
  storeId: number;
  storeCode: string;
  storeName: string;
  supplierName: string;
  receiptNumber: string;
  receiptDate: string;
  status: PurchaseReceiptStatus;
  totalAmount: number;
  lines: PurchaseReceiptLine[];
  createdDate: string;
  updatedDate: string;
}

export interface PurchaseReceiptListFilter {
  storeId?: number;
  status?: PurchaseReceiptStatus;
}
