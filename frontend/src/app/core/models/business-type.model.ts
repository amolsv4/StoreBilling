/** Read-only reference data in this phase - seeded server-side, no create/edit screen. */
export interface BusinessType {
  id: number;
  code: string;
  name: string;
  description: string;
  active: boolean;
}
