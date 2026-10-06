export interface PriceListBaseFields {
  organizationId: number;
  code: string;
  name: string;
  description: string;
  effectiveFrom: string | null;
  effectiveTo: string | null;
}

export type PriceListCreateRequest = PriceListBaseFields;

/** `organizationId` and `code` are immutable after creation and excluded from the update payload. */
export interface PriceListUpdateRequest {
  name: string;
  description: string;
  effectiveFrom: string | null;
  effectiveTo: string | null;
  active: boolean;
}

export interface PriceList extends PriceListBaseFields {
  id: number;
  active: boolean;
  organizationCode: string;
  organizationName: string;
  createdDate: string;
  updatedDate: string;
}
