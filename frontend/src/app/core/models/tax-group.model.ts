import { TaxComponent } from './tax-component.model';

/** Matches backend `CreateTaxGroupRequest`. */
export interface TaxGroupCreateRequest {
  organizationId: number;
  code: string;
  name: string;
  description: string;
  componentIds: number[];
}

/**
 * Matches backend `UpdateTaxGroupRequest`: `organizationId` and `code` are immutable after creation.
 * `inclusive` can only be set on update - it switches the group between tax-inclusive pricing
 * (tax is carved out of the shelf price) and tax-exclusive pricing (tax is added on top).
 */
export interface TaxGroupUpdateRequest {
  name: string;
  description: string;
  componentIds: number[];
  active: boolean;
  inclusive: boolean;
}

/** Mirrors backend `TaxGroupResponse`; the member components come back as full objects, not IDs. */
export interface TaxGroup {
  id: number;
  organizationId: number;
  organizationCode: string;
  organizationName: string;
  code: string;
  name: string;
  description: string;
  components: TaxComponent[];
  /** Server-computed sum of the components' percentages; display-only, never sent in a request. */
  totalPercentage: number;
  active: boolean;
  inclusive: boolean;
  createdDate: string;
  updatedDate: string;
}
