export interface ProductPriceCreateRequest {
  priceListId: number;
  productId: number;
  basePrice: number;
  sellingPrice: number;
  effectiveFrom: string | null;
  effectiveTo: string | null;
}

/** `priceListId` and `productId` are immutable after creation and excluded from the update payload. */
export interface ProductPriceUpdateRequest {
  basePrice: number;
  sellingPrice: number;
  effectiveFrom: string | null;
  effectiveTo: string | null;
  active: boolean;
}

export interface ProductPrice {
  id: number;
  priceListId: number;
  productId: number;
  basePrice: number;
  sellingPrice: number;
  effectiveFrom: string | null;
  effectiveTo: string | null;
  active: boolean;
  priceListCode: string;
  priceListName: string;
  productSku: string;
  productName: string;
  createdDate: string;
  updatedDate: string;
}
