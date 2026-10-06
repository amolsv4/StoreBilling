import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, defer, map, of, shareReplay, switchMap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { CurrentUser, JwtClaims, LoginRequest, LoginResponse } from '../models/auth.model';
import { RoleService } from './role.service';
import { TokenService } from './token.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenService = inject(TokenService);
  private readonly roleService = inject(RoleService);
  private readonly router = inject(Router);

  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  private readonly _currentUser = signal<CurrentUser | null>(null);
  /** Exposed as a read-only signal, per the phase spec. */
  readonly currentUser = this._currentUser.asReadonly();

  readonly isAuthenticated = computed(() => {
    const token = this.tokenService.getToken();
    return !!token && !this.isTokenExpired(token) && !!this._currentUser();
  });

  /**
   * Completes once the permission set for a restored session (page refresh / deep link) has been
   * derived. Route guards wait on this: without it they run against an empty permission list and
   * bounce a hard-loaded `/admin/...` URL back to `/dashboard`. Already complete when there was no
   * session to restore, or after a normal login (which awaits `loadPermissions()` itself).
   */
  private permissionsReady$: Observable<unknown> = of(null);

  constructor() {
    this.restoreSession();
  }

  permissionsReady(): Observable<unknown> {
    return this.permissionsReady$;
  }

  /**
   * Login against the backend. On success, stores the token and derives the effective permission
   * set (see loadPermissions) before the observable completes, so that by the time the caller
   * navigates to a guarded route the permission-aware guards/nav already have data to work with.
   */
  login(request: LoginRequest): Observable<CurrentUser> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.baseUrl}/login`, request).pipe(
      switchMap((res) => {
        const login = res.data;
        this.tokenService.setToken(login.accessToken);
        // The backend's login payload is flat and carries no email/name, so those stay blank
        // (same as after a page-refresh session restore) - the shell falls back to `username`.
        const current: CurrentUser = {
          id: login.userId,
          username: login.username,
          email: '',
          firstName: '',
          lastName: '',
          roles: login.roles ?? [],
          organizationId: login.organizationId ?? null,
          storeAccess: login.storeAccess ?? null,
          allStores: login.allStoreAccess,
          permissions: []
        };
        this._currentUser.set(current);
        return this.loadPermissions();
      })
    );
  }

  logout(): void {
    this.http.post<ApiResponse<null>>(`${this.baseUrl}/logout`, {}).subscribe({
      complete: () => this.finishLogout(),
      error: () => this.finishLogout()
    });
  }

  /** Clears client-side session state without calling the backend - used by the error interceptor on 401. */
  clearSession(): void {
    this.tokenService.clearToken();
    this._currentUser.set(null);
  }

  hasPermission(permissionCode: string): boolean {
    return this._currentUser()?.permissions.includes(permissionCode) ?? false;
  }

  hasAnyPermission(permissionCodes: string[]): boolean {
    return permissionCodes.some((code) => this.hasPermission(code));
  }

  private finishLogout(): void {
    this.clearSession();
    this.router.navigate(['/login']);
  }

  /**
   * Derives the current user's effective permissions from their role codes.
   *
   * DEVIATION NOTE: the backend contract's login response/JWT only carry `roles: string[]` (role
   * codes) - there is no "my permissions" endpoint. GET /role returns each role's full
   * `permissions: string[]`, so we fetch the role catalog and union the permission codes for every
   * role the user holds. This does mean the nav/guards are only as fresh as the last role fetch
   * (i.e. a role's permissions changing server-side won't reflect until next login/refresh) - an
   * acceptable trade-off for a Phase 1 POC.
   */
  private loadPermissions(): Observable<CurrentUser> {
    const user = this._currentUser();
    if (!user) {
      return of(null as unknown as CurrentUser);
    }
    return this.roleService.listAll().pipe(
      map((roles) => {
        const permissionSet = new Set<string>();
        for (const role of roles) {
          if (user.roles.includes(role.code)) {
            role.permissions.forEach((p) => permissionSet.add(p));
          }
        }
        const updated: CurrentUser = { ...user, permissions: Array.from(permissionSet) };
        this._currentUser.set(updated);
        return updated;
      }),
      catchError((err) => {
        // If the role lookup fails, don't block login entirely - the user is authenticated but will
        // simply see no permission-gated nav items/routes until the next successful refresh. Log it
        // loudly though: this used to swallow a real response-shape bug with no trace at all.
        console.error('Failed to derive permissions from /roles; the nav will be empty.', err);
        return of(user);
      })
    );
  }

  /** Reconstructs a session from a previously-stored token (e.g. on page refresh). */
  private restoreSession(): void {
    const token = this.tokenService.getToken();
    if (!token || this.isTokenExpired(token)) {
      this.tokenService.clearToken();
      return;
    }
    const claims = this.decodeToken(token);
    if (!claims) {
      this.tokenService.clearToken();
      return;
    }
    // The login response body (with firstName/lastName/email) is gone after a refresh, so rebuild a
    // minimal CurrentUser from the JWT claims. Names/email are left blank; the shell only relies on
    // username/roles/permissions, so this is an acceptable POC limitation.
    const restored: CurrentUser = {
      id: claims.userId,
      username: claims.sub,
      email: '',
      firstName: '',
      lastName: '',
      roles: claims.roles ?? [],
      organizationId: claims.organizationId ?? null,
      storeAccess: claims.storeAccess ?? null,
      allStores: claims.allStores ?? false,
      permissions: []
    };
    this._currentUser.set(restored);
    // The /roles request must NOT start while this constructor is still running: the HTTP interceptors
    // inject AuthService, so firing it synchronously here is a circular dependency (NG0200) and the
    // permissions would silently never load after a refresh. `defer` + a microtask starts it right after
    // construction; shareReplay lets the route guards join the same single request.
    const ready$ = defer(() => this.loadPermissions()).pipe(shareReplay(1));
    this.permissionsReady$ = ready$;
    queueMicrotask(() => ready$.subscribe());
  }

  private decodeToken(token: string): JwtClaims | null {
    try {
      const payload = token.split('.')[1];
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const json = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(json) as JwtClaims;
    } catch {
      return null;
    }
  }

  private isTokenExpired(token: string): boolean {
    const claims = this.decodeToken(token);
    if (!claims?.exp) {
      return true;
    }
    return Date.now() >= claims.exp * 1000;
  }
}
