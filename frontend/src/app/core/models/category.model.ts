export interface CategoryBaseFields {
  organizationId: number;
  code: string;
  name: string;
  description: string;
  parentCategoryId: number | null;
}

export type CategoryCreateRequest = CategoryBaseFields;

/** `organizationId` and `code` are immutable after creation and excluded from the update payload. */
export interface CategoryUpdateRequest {
  name: string;
  description: string;
  parentCategoryId: number | null;
  active: boolean;
}

export interface Category extends CategoryBaseFields {
  id: number;
  active: boolean;
  organizationCode: string;
  organizationName: string;
  parentCategoryCode: string | null;
  parentCategoryName: string | null;
  createdDate: string;
  updatedDate: string;
}
