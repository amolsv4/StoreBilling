export interface OrganizationBaseFields {
  code: string;
  name: string;
  legalName: string;
  taxRegistrationNo: string;
  addressLine1: string;
  addressLine2: string;
  city: string;
  state: string;
  country: string;
  postalCode: string;
  contactEmail: string;
  contactPhone: string;
}

export type OrganizationCreateRequest = OrganizationBaseFields;

/** Matches backend `UpdateOrganizationRequest` - the code is fixed at creation. */
export interface OrganizationUpdateRequest extends Omit<OrganizationBaseFields, 'code'> {
  active: boolean;
}

export interface Organization extends OrganizationBaseFields {
  id: number;
  active: boolean;
  createdDate: string;
  updatedDate: string;
}
