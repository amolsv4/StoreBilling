import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, map, of } from 'rxjs';
import { PosSession } from '../models/pos-session.model';
import { Store } from '../models/store.model';
import { Terminal } from '../models/terminal.model';
import { AuthService } from './auth.service';
import { StoreService } from './store.service';
import { TerminalService } from './terminal.service';

const STORAGE_KEY = 'pos.session';

/**
 * Signal-based store/terminal session for the POS area. Seeded from `AuthService.currentUser()`'s
 * `storeAccess`/`allStores`, persisted to `sessionStorage` (not `localStorage` - a shift-scoped
 * choice shouldn't silently survive to a different day, unlike the JWT) with a `userId` guard on
 * restore so a different user logging into the same untouched tab never inherits a stale pick
 * (plan §5).
 */
@Injectable({ providedIn: 'root' })
export class PosSessionService {
  // Declared before `_session` below: its initializer calls `restore()`, which reads
  // `this.authService` - field initializers run in declaration order, so `authService` must be
  // assigned first (same ordering pitfall as the MAT_DIALOG_DATA-as-field convention elsewhere).
  private readonly authService = inject(AuthService);
  private readonly storeService = inject(StoreService);
  private readonly terminalService = inject(TerminalService);

  private readonly _session = signal<PosSession | null>(this.restore());
  readonly session = this._session.asReadonly();
  readonly hasSession = computed(() => this._session() !== null);

  /** Active stores the current user may operate a terminal in: all active stores in their
   *  organization if `allStores`, else only the ones listed in `storeAccess`. A user with no
   *  organization of their own (the bootstrap super admin) isn't org-scoped, so `allStores` then
   *  means every active store. */
  accessibleStores(): Observable<Store[]> {
    const user = this.authService.currentUser();
    if (!user) {
      return of([]);
    }
    return this.storeService.listAll().pipe(
      map((stores) =>
        stores.filter(
          (store) =>
            store.active &&
            (user.organizationId == null || store.organizationId === user.organizationId) &&
            (user.allStores || (user.storeAccess ?? []).includes(store.id))
        )
      )
    );
  }

  /** Active terminals belonging to the given store. */
  terminalsForStore(storeId: number): Observable<Terminal[]> {
    return this.terminalService
      .listAll()
      .pipe(map((terminals) => terminals.filter((terminal) => terminal.active && terminal.storeId === storeId)));
  }

  setSession(store: Store, terminal: Terminal): void {
    const user = this.authService.currentUser();
    if (!user) {
      return;
    }
    const session: PosSession = {
      organizationId: store.organizationId,
      storeId: store.id,
      storeName: store.name,
      terminalId: terminal.id,
      terminalName: terminal.name,
      userId: user.id
    };
    this._session.set(session);
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  }

  /** Ends the session (e.g. "Switch Terminal") without touching the auth session. */
  clear(): void {
    this._session.set(null);
    sessionStorage.removeItem(STORAGE_KEY);
  }

  private restore(): PosSession | null {
    try {
      const raw = sessionStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return null;
      }
      const parsed = JSON.parse(raw) as PosSession;
      const currentUserId = this.authService.currentUser()?.id;
      // A session persisted before `organizationId` was added can't resolve its org - make them re-pick.
      if (!currentUserId || parsed.userId !== currentUserId || parsed.organizationId == null) {
        sessionStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return parsed;
    } catch {
      sessionStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }
}
