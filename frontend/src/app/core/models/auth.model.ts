export interface LoginRequest {
  username: string;
  password: string;
}

/** The `user` object nested inside the login response payload. */
export interface LoginResponseUser {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  organizationId: number | null;
  storeAccess: number[] | null;
  allStores: boolean;
}

/**
 * Mirrors the backend's `auth/dto/LoginResponse.java` exactly - a FLAT record, not a nested `user`
 * object. (An earlier version of this interface assumed a nested `user`; against the real backend
 * that made `user` undefined, so the signed-in user had no roles and every permission-gated nav item
 * silently disappeared.) Note the backend has no email/firstName/lastName in this payload.
 */
export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresInMs: number;
  userId: number;
  username: string;
  roles: string[];
  organizationId: number | null;
  allStoreAccess: boolean;
  storeAccess: number[];
}

/**
 * The claims embedded in the JWT, per the contract. Decoded client-side only to restore a session
 * after a page refresh (the login response body is preferred whenever it is available, since it is
 * guaranteed to exist and carries richer user info than the token claims).
 */
export interface JwtClaims {
  sub: string;
  userId: number;
  roles: string[];
  organizationId?: number | null;
  storeAccess?: number[] | null;
  allStores?: boolean;
  exp: number;
  iat: number;
  jti: string;
}

/**
 * The application's in-memory view of the signed-in user. Extends the login response's user object
 * with a `permissions` array.
 *
 * NOTE: the backend contract's login response and JWT claims only carry `roles` (role codes), not
 * resolved permission codes. There is no endpoint that returns "my permissions" directly. To make the
 * permission-aware nav/guards work, AuthService derives `permissions` client-side by fetching all
 * roles (GET /role, which includes each role's `permissions: string[]`) and unioning the permission
 * codes for the roles the current user holds. See AuthService.loadPermissions().
 */
export interface CurrentUser extends LoginResponseUser {
  permissions: string[];
}
