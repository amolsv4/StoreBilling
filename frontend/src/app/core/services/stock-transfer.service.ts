import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import {
  StockTransferHeaderRequest,
  StockTransferHeaderUpdateRequest,
  StockTransferLineRequest,
  StockTransferListFilter,
  StockTransferResponse
} from '../models/stock-transfer.model';

/** Same incremental header-then-lines shape as Purchase Receipts; `complete()` replaces `receive()`
 *  and checks store access for both the from- and to-store (plan §3). */
@Injectable({ providedIn: 'root' })
export class StockTransferService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/stock-transfers`;

  list(filter: StockTransferListFilter = {}, page = 0, size = 10): Observable<PageResponse<StockTransferResponse>> {
    const params: Record<string, string | number> = { page, size };
    if (filter.storeId != null) {
      params['storeId'] = filter.storeId;
    }
    if (filter.status) {
      params['status'] = filter.status;
    }
    return this.http
      .get<ApiResponse<PageResponse<StockTransferResponse>>>(this.baseUrl, { params })
      .pipe(map((res) => res.data));
  }

  getById(id: number): Observable<StockTransferResponse> {
    return this.http.get<ApiResponse<StockTransferResponse>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  create(payload: StockTransferHeaderRequest): Observable<StockTransferResponse> {
    return this.http.post<ApiResponse<StockTransferResponse>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  updateHeader(id: number, payload: StockTransferHeaderUpdateRequest): Observable<StockTransferResponse> {
    return this.http
      .put<ApiResponse<StockTransferResponse>>(`${this.baseUrl}/${id}`, payload)
      .pipe(map((res) => res.data));
  }

  deleteHeader(id: number): Observable<void> {
    return this.http.delete<ApiResponse<null>>(`${this.baseUrl}/${id}`).pipe(map(() => undefined));
  }

  addLine(id: number, payload: StockTransferLineRequest): Observable<StockTransferResponse> {
    return this.http
      .post<ApiResponse<StockTransferResponse>>(`${this.baseUrl}/${id}/lines`, payload)
      .pipe(map((res) => res.data));
  }

  updateLine(id: number, lineId: number, payload: StockTransferLineRequest): Observable<StockTransferResponse> {
    return this.http
      .put<ApiResponse<StockTransferResponse>>(`${this.baseUrl}/${id}/lines/${lineId}`, payload)
      .pipe(map((res) => res.data));
  }

  removeLine(id: number, lineId: number): Observable<StockTransferResponse> {
    return this.http
      .delete<ApiResponse<StockTransferResponse>>(`${this.baseUrl}/${id}/lines/${lineId}`)
      .pipe(map((res) => res.data));
  }

  complete(id: number): Observable<StockTransferResponse> {
    return this.http
      .post<ApiResponse<StockTransferResponse>>(`${this.baseUrl}/${id}/complete`, {})
      .pipe(map((res) => res.data));
  }
}
