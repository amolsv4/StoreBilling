import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { Role, RoleCreateRequest, RoleUpdateRequest } from '../models/role.model';

/**
 * The real backend `RoleResponse.permissions` is `List<PermissionResponse>` (objects with
 * id/code/name/module/description), NOT a plain `string[]` - confirmed by reading
 * `role/dto/RoleResponse.java` directly. Every frontend consumer of `Role.permissions`
 * (AuthService's permission-set derivation, the Role list's display column, the Role edit
 * dialog's pre-selected checkboxes) was written against the `string[]` shape declared in
 * `role.model.ts`, so this raw backend shape is normalized to just the `code` strings right here,
 * once, rather than fixing every consumer individually.
 */
interface RawRolePermission {
  code: string;
}

interface RawRole extends Omit<Role, 'permissions'> {
  permissions: RawRolePermission[];
}

function normalizeRole(raw: RawRole): Role {
  return { ...raw, permissions: raw.permissions.map((p) => p.code) };
}

@Injectable({ providedIn: 'root' })
export class RoleService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/roles`;

  list(page = 0, size = 10): Observable<PageResponse<Role>> {
    return this.http
      .get<ApiResponse<PageResponse<RawRole>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((res) => ({ ...res.data, content: res.data.content.map(normalizeRole) })));
  }

  /** Used by AuthService to resolve the current user's effective permissions (see auth.service.ts). */
  listAll(): Observable<Role[]> {
    return this.list(0, 200).pipe(map((page) => page.content));
  }

  getById(id: number): Observable<Role> {
    return this.http
      .get<ApiResponse<RawRole>>(`${this.baseUrl}/${id}`)
      .pipe(map((res) => normalizeRole(res.data)));
  }

  create(payload: RoleCreateRequest): Observable<Role> {
    return this.http
      .post<ApiResponse<RawRole>>(this.baseUrl, payload)
      .pipe(map((res) => normalizeRole(res.data)));
  }

  update(id: number, payload: RoleUpdateRequest): Observable<Role> {
    return this.http
      .put<ApiResponse<RawRole>>(`${this.baseUrl}/${id}`, payload)
      .pipe(map((res) => normalizeRole(res.data)));
  }
}
