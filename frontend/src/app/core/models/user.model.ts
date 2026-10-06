/** Matches backend `CreateUserRequest`. The initial password is set here; there is no change-password endpoint yet. */
export interface UserCreateRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone: string;
  organizationId: number | null;
  allStoreAccess: boolean;
  storeIds: number[];
  roleCodes: string[];
}

/** Matches backend `UpdateUserRequest` - username, email and password are not updatable. */
export interface UserUpdateRequest {
  firstName: string;
  lastName: string;
  phone: string;
  organizationId: number | null;
  allStoreAccess: boolean;
  active: boolean;
  accountLocked: boolean;
  storeIds: number[];
  roleCodes: string[];
}

/** Mirrors backend `UserResponse`. Named `AppUser` (not `User`) to avoid clashing with the DOM's global `User` type. */
export interface AppUser {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  organizationId: number | null;
  allStoreAccess: boolean;
  active: boolean;
  accountLocked: boolean;
  lastLoginDate: string | null;
  roleCodes: string[];
  storeIds: number[];
  createdDate: string;
  updatedDate: string;
}
