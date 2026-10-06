export interface CustomerCreateRequest {
  organizationId: number;
  code: string;
  name: string;
  mobile: string;
  email: string;
  addressLine1: string;
  addressLine2: string;
  city: string;
  state: string;
  country: string;
  postalCode: string;
  taxRegistrationNo: string;
  creditLimit: number;
}

/** `code` and `organizationId` are immutable after creation and excluded from the update payload. */
export interface CustomerUpdateRequest {
  name: string;
  mobile: string;
  email: string;
  addressLine1: string;
  addressLine2: string;
  city: string;
  state: string;
  country: string;
  postalCode: string;
  taxRegistrationNo: string;
  creditLimit: number;
  active: boolean;
}

export interface Customer {
  id: number;
  organizationId: number;
  code: string;
  name: string;
  mobile: string;
  email: string;
  addressLine1: string;
  addressLine2: string;
  city: string;
  state: string;
  country: string;
  postalCode: string;
  taxRegistrationNo: string;
  creditLimit: number;
  /** Response-only; accrued server-side, never part of the create/update request. */
  loyaltyPoints: number;
  active: boolean;
  organizationCode: string;
  organizationName: string;
  createdDate: string;
  updatedDate: string;
}
