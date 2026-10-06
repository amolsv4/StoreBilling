/**
 * The cashier's active store/terminal pick for the current tab. Persisted to `sessionStorage` (not
 * `localStorage`) - a shift-scoped choice shouldn't silently survive to a different day the way the
 * JWT does (plan §5).
 */
export interface PosSession {
  /** The picked store's organization - the effective org for product/customer lookups, since an
   *  org-less user (e.g. the bootstrap super admin) has no organization of their own. */
  organizationId: number;
  storeId: number;
  storeName: string;
  terminalId: number;
  terminalName: string;
  /** Guards `sessionStorage` restore: a different user reusing the same untouched tab must never
   *  inherit a stale pick (plan §5). */
  userId: number;
}
