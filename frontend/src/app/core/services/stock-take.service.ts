import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import {
  StockTakeHeaderRequest,
  StockTakeHeaderUpdateRequest,
  StockTakeLineAddRequest,
  StockTakeLineUpdateRequest,
  StockTakeListFilter,
  StockTakeResponse
} from '../models/stock-take.model';

/** Same incremental header-then-lines shape; the server snapshots `systemQuantity` per line at
 *  add-time, and `complete()` re-diffs against the *live* balance rather than the stale draft-time
 *  snapshot (plan §3, stock-take completion algorithm). */
@Injectable({ providedIn: 'root' })
export class StockTakeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/stock-takes`;

  list(filter: StockTakeListFilter = {}, page = 0, size = 10): Observable<PageResponse<StockTakeResponse>> {
    const params: Record<string, string | number> = { page, size };
    if (filter.storeId != null) {
      params['storeId'] = filter.storeId;
    }
    if (filter.status) {
      params['status'] = filter.status;
    }
    return this.http
      .get<ApiResponse<PageResponse<StockTakeResponse>>>(this.baseUrl, { params })
      .pipe(map((res) => res.data));
  }

  getById(id: number): Observable<StockTakeResponse> {
    return this.http.get<ApiResponse<StockTakeResponse>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  create(payload: StockTakeHeaderRequest): Observable<StockTakeResponse> {
    return this.http.post<ApiResponse<StockTakeResponse>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  updateHeader(id: number, payload: StockTakeHeaderUpdateRequest): Observable<StockTakeResponse> {
    return this.http
      .put<ApiResponse<StockTakeResponse>>(`${this.baseUrl}/${id}`, payload)
      .pipe(map((res) => res.data));
  }

  deleteHeader(id: number): Observable<void> {
    return this.http.delete<ApiResponse<null>>(`${this.baseUrl}/${id}`).pipe(map(() => undefined));
  }

  addLine(id: number, payload: StockTakeLineAddRequest): Observable<StockTakeResponse> {
    return this.http
      .post<ApiResponse<StockTakeResponse>>(`${this.baseUrl}/${id}/lines`, payload)
      .pipe(map((res) => res.data));
  }

  updateLine(id: number, lineId: number, payload: StockTakeLineUpdateRequest): Observable<StockTakeResponse> {
    return this.http
      .put<ApiResponse<StockTakeResponse>>(`${this.baseUrl}/${id}/lines/${lineId}`, payload)
      .pipe(map((res) => res.data));
  }

  removeLine(id: number, lineId: number): Observable<StockTakeResponse> {
    return this.http
      .delete<ApiResponse<StockTakeResponse>>(`${this.baseUrl}/${id}/lines/${lineId}`)
      .pipe(map((res) => res.data));
  }

  complete(id: number): Observable<StockTakeResponse> {
    return this.http
      .post<ApiResponse<StockTakeResponse>>(`${this.baseUrl}/${id}/complete`, {})
      .pipe(map((res) => res.data));
  }
}
