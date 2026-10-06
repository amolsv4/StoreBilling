export type TerminalType = 'POS' | 'MPOS' | 'KIOSK';

export const TERMINAL_TYPES: TerminalType[] = ['POS', 'MPOS', 'KIOSK'];

/** Matches backend `CreateTerminalRequest`. */
export interface TerminalCreateRequest {
  storeId: number;
  code: string;
  name: string;
  terminalType: TerminalType;
  deviceIdentifier: string;
}

/** Matches backend `UpdateTerminalRequest` - the store and code are fixed at creation. */
export interface TerminalUpdateRequest {
  name: string;
  terminalType: TerminalType;
  deviceIdentifier: string;
  active: boolean;
}

/** Mirrors backend `TerminalResponse`. */
export interface Terminal {
  id: number;
  storeId: number;
  storeCode: string;
  code: string;
  name: string;
  terminalType: TerminalType;
  deviceIdentifier: string | null;
  active: boolean;
  createdDate: string;
  updatedDate: string;
}
