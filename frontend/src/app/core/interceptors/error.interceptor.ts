import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ApiError } from '../models/api-response.model';

/**
 * Unwraps the backend's `ApiError` envelope, surfaces it via a MatSnackBar, and redirects to
 * /login on 401 (clearing the stored session first).
 *
 * The failing /auth/login request itself is deliberately excluded from the generic
 * snackbar/redirect handling: a bad-password 401 there is not a "session expired" event (there was
 * no session yet), and the login component already reads `err.error.message` to show the error
 * inline - a duplicate snackbar would be redundant.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  const router = inject(Router);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const isLoginRequest = req.url.endsWith('/auth/login');
      const body = error.error as ApiError | undefined;
      const message = body && body.success === false && body.message ? body.message : error.message || 'An unexpected error occurred.';

      if (!isLoginRequest) {
        if (error.status === 401) {
          authService.clearSession();
          snackBar.open('Your session has expired. Please log in again.', 'Dismiss', { duration: 5000 });
          router.navigate(['/login']);
        } else {
          snackBar.open(message, 'Dismiss', { duration: 5000 });
        }
      }

      return throwError(() => error);
    })
  );
};
