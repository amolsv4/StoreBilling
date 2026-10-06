import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { PosSessionService } from '../services/pos-session.service';

/**
 * Mirrors `permissionGuard`'s style: redirects to `/pos/session` when no store/terminal has been
 * picked yet for this tab. Used on `/pos/sale` and `/pos/held-bills` so a cashier can never reach
 * the sale screen or held-bills list without an active session (plan §5).
 */
export const posSessionGuard: CanActivateFn = () => {
  const posSessionService = inject(PosSessionService);
  const router = inject(Router);

  if (posSessionService.hasSession()) {
    return true;
  }

  return router.createUrlTree(['/pos/session']);
};
