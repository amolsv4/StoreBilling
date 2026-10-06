import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import {
  PurchaseReceiptHeaderRequest,
  PurchaseReceiptHeaderUpdateRequest,
  PurchaseReceiptLineRequest,
  PurchaseReceiptListFilter,
  PurchaseReceiptResponse
} from '../models/purchase-receipt.model';

/**
 * Incremental header-then-lines model (plan reconciliation note 6): create the header, then add
 * lines one at a time; everything is editable/hard-deletable while DRAFT, immutable once RECEIVED.
 * Every mutating call (including line add/update/remove) returns the full updated header+lines,
 * mirroring Sales' response convention.
 */
@Injectable({ providedIn: 'root' })
export class PurchaseReceiptService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/purchase-receipts`;

  list(filter: PurchaseReceiptListFilter = {}, page = 0, size = 10): Observable<PageResponse<PurchaseReceiptResponse>> {
    const params: Record<string, string | number> = { page, size };
    if (filter.storeId != null) {
      params['storeId'] = filter.storeId;
    }
    if (filter.status) {
      params['status'] = filter.status;
    }
    return this.http
      .get<ApiResponse<PageResponse<PurchaseReceiptResponse>>>(this.baseUrl, { params })
      .pipe(map((res) => res.data));
  }

  getById(id: number): Observable<PurchaseReceiptResponse> {
    return this.http.get<ApiResponse<PurchaseReceiptResponse>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  create(payload: PurchaseReceiptHeaderRequest): Observable<PurchaseReceiptResponse> {
    return this.http.post<ApiResponse<PurchaseReceiptResponse>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  updateHeader(id: number, payload: PurchaseReceiptHeaderUpdateRequest): Observable<PurchaseReceiptResponse> {
    return this.http
      .put<ApiResponse<PurchaseReceiptResponse>>(`${this.baseUrl}/${id}`, payload)
      .pipe(map((res) => res.data));
  }

  /** Hard delete - only valid while DRAFT (nothing has touched the ledger yet). */
  deleteHeader(id: number): Observable<void> {
    return this.http.delete<ApiResponse<null>>(`${this.baseUrl}/${id}`).pipe(map(() => undefined));
  }

  addLine(id: number, payload: PurchaseReceiptLineRequest): Observable<PurchaseReceiptResponse> {
    return this.http
      .post<ApiResponse<PurchaseReceiptResponse>>(`${this.baseUrl}/${id}/lines`, payload)
      .pipe(map((res) => res.data));
  }

  updateLine(id: number, lineId: number, payload: PurchaseReceiptLineRequest): Observable<PurchaseReceiptResponse> {
    return this.http
      .put<ApiResponse<PurchaseReceiptResponse>>(`${this.baseUrl}/${id}/lines/${lineId}`, payload)
      .pipe(map((res) => res.data));
  }

  /** Hard delete - only valid while DRAFT (an in-progress line was never a finalized record). */
  removeLine(id: number, lineId: number): Observable<PurchaseReceiptResponse> {
    return this.http
      .delete<ApiResponse<PurchaseReceiptResponse>>(`${this.baseUrl}/${id}/lines/${lineId}`)
      .pipe(map((res) => res.data));
  }

  receive(id: number): Observable<PurchaseReceiptResponse> {
    return this.http
      .post<ApiResponse<PurchaseReceiptResponse>>(`${this.baseUrl}/${id}/receive`, {})
      .pipe(map((res) => res.data));
  }
}
