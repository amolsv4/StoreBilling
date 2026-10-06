import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Reads the route's `data.permission` (a single permission code, e.g. 'ORGANIZATION_VIEW') and
 * checks it against the current user's derived permissions. Routes without a `data.permission`
 * entry are allowed through (this guard only enforces module-level view permissions on admin
 * routes; it is not a substitute for server-side authorization, which the backend still enforces).
 *
 * Waits for `AuthService.permissionsReady()` first, so a hard refresh or deep link doesn't get
 * judged against the not-yet-loaded (empty) permission list and bounced to /dashboard.
 */
export const permissionGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const requiredPermission = route.data?.['permission'] as string | undefined;
  if (!requiredPermission) {
    return true;
  }

  return authService
    .permissionsReady()
    .pipe(map(() => (authService.hasPermission(requiredPermission) ? true : router.createUrlTree(['/dashboard']))));
};
