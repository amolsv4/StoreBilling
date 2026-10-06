export type Uom = 'PCS' | 'KG' | 'GRAM' | 'LITER' | 'BOX' | 'PACK';

export const UOM_OPTIONS: Uom[] = ['PCS', 'KG', 'GRAM', 'LITER', 'BOX', 'PACK'];

export interface ProductCreateRequest {
  organizationId: number;
  categoryId: number;
  taxGroupId: number | null;
  sku: string;
  barcode: string | null;
  name: string;
  description: string;
  brand: string;
  uom: Uom;
  saleEnabled: boolean;
  purchaseEnabled: boolean;
}

/** `sku` and `organizationId` are immutable after creation and excluded from the update payload. */
export interface ProductUpdateRequest {
  categoryId: number;
  taxGroupId: number | null;
  barcode: string | null;
  name: string;
  description: string;
  brand: string;
  uom: Uom;
  saleEnabled: boolean;
  purchaseEnabled: boolean;
  active: boolean;
}

export interface Product {
  id: number;
  organizationId: number;
  categoryId: number;
  taxGroupId: number | null;
  sku: string;
  barcode: string | null;
  name: string;
  description: string;
  brand: string;
  uom: Uom;
  saleEnabled: boolean;
  purchaseEnabled: boolean;
  active: boolean;
  organizationCode: string;
  organizationName: string;
  categoryCode: string;
  categoryName: string;
  taxGroupCode: string | null;
  taxGroupName: string | null;
  createdDate: string;
  updatedDate: string;
}
