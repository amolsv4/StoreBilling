import { Injectable } from '@angular/core';

const TOKEN_KEY = 'sb_access_token';

/**
 * Stores the JWT access token in localStorage.
 *
 * SECURITY NOTE: localStorage is acceptable for this POC but is readable by any script running on
 * the page, which makes it vulnerable to token theft via XSS. A production build should prefer an
 * httpOnly, Secure, SameSite cookie issued by the backend so the token is never exposed to page JS.
 * Revisit this before shipping beyond Phase 1.
 */
@Injectable({ providedIn: 'root' })
export class TokenService {
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  clearToken(): void {
    localStorage.removeItem(TOKEN_KEY);
  }
}
