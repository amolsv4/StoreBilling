export type SaleStatus = 'DRAFT' | 'HELD' | 'COMPLETED';

export type PaymentMethod = 'CASH' | 'CARD' | 'UPI' | 'WALLET' | 'BANK_TRANSFER' | 'CREDIT' | 'GIFT_CARD' | 'OTHER';

export const PAYMENT_METHODS: PaymentMethod[] = [
  'CASH',
  'CARD',
  'UPI',
  'WALLET',
  'BANK_TRANSFER',
  'CREDIT',
  'GIFT_CARD',
  'OTHER'
];

export type NotificationChannel = 'EMAIL' | 'SMS' | 'WHATSAPP';

export const NOTIFICATION_CHANNELS: NotificationChannel[] = ['EMAIL', 'SMS', 'WHATSAPP'];

export interface SaleCreateRequest {
  storeId: number;
  terminalId: number;
  customerId?: number | null;
}

/** Snapshot fields (productName/sku/uom/taxGroupId/taxPercentage) never change after the line is
 *  added, even if the product/tax-group is edited later - the invoice must stay historically accurate. */
export interface SaleLine {
  id: number;
  productId: number;
  productName: string;
  sku: string;
  quantity: number;
  uom: string;
  unitPrice: number;
  priceOverridden: boolean;
  discountAmount: number;
  taxGroupId: number | null;
  taxPercentage: number;
  taxAmount: number;
  lineTotal: number;
}

/** Full sale header+lines. Every mutating sales endpoint returns this shape - the frontend renders
 *  it as-is and never recomputes subtotal/tax/total client-side. */
export interface SaleResponse {
  id: number;
  organizationId: number;
  storeId: number;
  terminalId: number;
  customerId: number | null;
  soldByUserId: number;
  status: SaleStatus;
  invoiceNumber: string | null;
  currency: string;
  subtotal: number;
  discountAmount: number;
  taxAmount: number;
  totalAmount: number;
  notes: string | null;
  lines: SaleLine[];
  saleDate: string;
  completedDate: string | null;
  createdDate: string;
  updatedDate: string;
}

export interface SaleLineAddRequest {
  productId: number;
  quantity: number;
  unitPrice?: number | null;
  discountAmount?: number | null;
}

export interface SaleLineUpdateRequest {
  quantity: number;
  unitPrice?: number | null;
  discountAmount?: number | null;
}

export interface SaleDiscountRequest {
  discountAmount: number;
}

/** Mirrors backend `CompleteSaleResponse`: the finalized sale (with its invoice number) nested, plus the tender summary. */
export interface CompleteSaleResponse {
  sale: SaleResponse;
  amountPaid: number;
  changeDue: number;
}

export interface PaymentCreateRequest {
  paymentMethod: PaymentMethod;
  amount: number;
  referenceNumber?: string | null;
}

export interface PaymentResponse {
  id: number;
  saleId: number;
  paymentMethod: PaymentMethod;
  amount: number;
  referenceNumber: string | null;
  status: string;
  createdDate: string;
}

export interface SaleNotificationRequest {
  channel: NotificationChannel;
}

/** Optional filters for GET /sales - all are query params, all optional. */
export interface SaleListFilter {
  status?: SaleStatus;
  storeId?: number;
  terminalId?: number;
}
