export type TaxType = 'CGST' | 'SGST' | 'IGST' | 'CESS' | 'GENERAL';

export const TAX_TYPES: TaxType[] = ['CGST', 'SGST', 'IGST', 'CESS', 'GENERAL'];

export interface TaxComponentBaseFields {
  organizationId: number;
  code: string;
  name: string;
  taxType: TaxType;
  percentage: number;
}

export type TaxComponentCreateRequest = TaxComponentBaseFields;

/** `organizationId` and `code` are immutable after creation and excluded from the update payload. */
export interface TaxComponentUpdateRequest {
  name: string;
  taxType: TaxType;
  percentage: number;
  active: boolean;
}

export interface TaxComponent extends TaxComponentBaseFields {
  id: number;
  active: boolean;
  organizationCode: string;
  organizationName: string;
  createdDate: string;
  updatedDate: string;
}
