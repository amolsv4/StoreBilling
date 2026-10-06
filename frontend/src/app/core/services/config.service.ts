import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import {
  ConfigCreateRequest,
  ConfigListParams,
  ConfigUpdateRequest,
  ConfigurationItem,
  ResolveParams,
  ResolvedConfig
} from '../models/configuration.model';

@Injectable({ providedIn: 'root' })
export class ConfigService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/config`;

  list(params: ConfigListParams): Observable<PageResponse<ConfigurationItem>> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 10));
    if (params.scopeType) {
      httpParams = httpParams.set('scopeType', params.scopeType);
    }
    if (params.scopeId != null) {
      httpParams = httpParams.set('scopeId', String(params.scopeId));
    }
    if (params.key) {
      httpParams = httpParams.set('key', params.key);
    }
    return this.http
      .get<ApiResponse<PageResponse<ConfigurationItem>>>(this.baseUrl, { params: httpParams })
      .pipe(map((res) => res.data));
  }

  create(payload: ConfigCreateRequest): Observable<ConfigurationItem> {
    return this.http.post<ApiResponse<ConfigurationItem>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  update(id: number, payload: ConfigUpdateRequest): Observable<ConfigurationItem> {
    return this.http.put<ApiResponse<ConfigurationItem>>(`${this.baseUrl}/${id}`, payload).pipe(map((res) => res.data));
  }

  /** GET /config/{key}?terminalId=&storeId=&organizationId=&businessTypeId= - the resolve panel's engine call. */
  resolve(key: string, params: ResolveParams): Observable<ResolvedConfig> {
    let httpParams = new HttpParams();
    if (params.terminalId != null) {
      httpParams = httpParams.set('terminalId', String(params.terminalId));
    }
    if (params.storeId != null) {
      httpParams = httpParams.set('storeId', String(params.storeId));
    }
    if (params.organizationId != null) {
      httpParams = httpParams.set('organizationId', String(params.organizationId));
    }
    if (params.businessTypeId != null) {
      httpParams = httpParams.set('businessTypeId', String(params.businessTypeId));
    }
    return this.http
      .get<ApiResponse<ResolvedConfig>>(`${this.baseUrl}/${encodeURIComponent(key)}`, { params: httpParams })
      .pipe(map((res) => res.data));
  }
}
