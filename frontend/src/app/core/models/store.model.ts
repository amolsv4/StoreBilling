interface StoreContactFields {
  timezone: string;
  addressLine1: string;
  addressLine2: string;
  city: string;
  state: string;
  country: string;
  postalCode: string;
  phone: string;
  email: string;
}

/** Matches backend `CreateStoreRequest`. */
export interface StoreCreateRequest extends StoreContactFields {
  organizationId: number;
  businessTypeId: number;
  code: string;
  name: string;
}

/**
 * Matches backend `UpdateStoreRequest`: organization, business type and code are fixed at creation.
 * `defaultPriceListId` is what the POS resolves prices from, so a store without one cannot sell.
 */
export interface StoreUpdateRequest extends StoreContactFields {
  name: string;
  active: boolean;
  defaultPriceListId: number | null;
}

/** Mirrors backend `StoreResponse`. */
export interface Store extends StoreContactFields {
  id: number;
  organizationId: number;
  organizationCode: string;
  businessTypeId: number;
  businessTypeCode: string;
  code: string;
  name: string;
  active: boolean;
  defaultPriceListId: number | null;
  createdDate: string;
  updatedDate: string;
}
