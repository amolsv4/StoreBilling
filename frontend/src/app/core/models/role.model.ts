/** Matches backend `CreateRoleRequest` - the role code is fixed at creation. */
export interface RoleCreateRequest {
  code: string;
  name: string;
  description: string;
  permissionCodes: string[];
}

/** Matches backend `UpdateRoleRequest` - no `code` and no active flag (roles cannot be deactivated). */
export interface RoleUpdateRequest {
  name: string;
  description: string;
  permissionCodes: string[];
}

/**
 * Mirrors backend `RoleResponse`. The backend sends `permissions` as objects; RoleService
 * normalizes them to just the permission codes, so the app-facing shape stays `string[]`.
 */
export interface Role {
  id: number;
  code: string;
  name: string;
  description: string;
  permissions: string[];
  systemRole: boolean;
}
