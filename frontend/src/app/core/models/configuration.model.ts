export type ScopeType = 'GLOBAL' | 'BUSINESS_TYPE' | 'ORGANIZATION' | 'STORE' | 'TERMINAL';
export type ConfigDataType = 'STRING' | 'BOOLEAN' | 'NUMBER' | 'JSON';

export const SCOPE_TYPES: ScopeType[] = ['GLOBAL', 'BUSINESS_TYPE', 'ORGANIZATION', 'STORE', 'TERMINAL'];
export const CONFIG_DATA_TYPES: ConfigDataType[] = ['STRING', 'BOOLEAN', 'NUMBER', 'JSON'];

export interface ConfigurationItem {
  id: number;
  configKey: string;
  configValue: string;
  dataType: ConfigDataType;
  scopeType: ScopeType;
  scopeId: number | null;
  /** Not sent by the backend (only `scopeId`); screens fall back to showing the id. */
  scopeName?: string | null;
  description: string | null;
  active: boolean;
  createdDate: string;
  updatedDate: string;
}

export interface ConfigCreateRequest {
  configKey: string;
  configValue: string;
  dataType: ConfigDataType;
  scopeType: ScopeType;
  scopeId: number | null;
  description: string | null;
}

/** PUT /config/{id} only accepts these three fields - configKey/scopeType/scopeId/dataType are immutable. */
export interface ConfigUpdateRequest {
  configValue: string;
  description: string | null;
  active: boolean;
}

export interface ConfigListParams {
  scopeType?: ScopeType;
  scopeId?: number;
  key?: string;
  page?: number;
  size?: number;
}

export interface ResolveParams {
  terminalId?: number | null;
  storeId?: number | null;
  organizationId?: number | null;
  businessTypeId?: number | null;
}

export interface ResolvedConfig {
  configKey: string;
  value: string;
  dataType: ConfigDataType;
  resolvedScopeType: ScopeType;
  resolvedScopeId: number | null;
}
