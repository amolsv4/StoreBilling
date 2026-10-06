import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import {
  CompleteSaleResponse,
  PaymentCreateRequest,
  PaymentResponse,
  SaleCreateRequest,
  SaleDiscountRequest,
  SaleLineAddRequest,
  SaleLineUpdateRequest,
  SaleListFilter,
  SaleNotificationRequest,
  SaleResponse
} from '../models/sale.model';

/**
 * Every mutating endpoint here returns the full updated `SaleResponse` (lines + server-computed
 * totals) per the reconciled contract - callers should always replace their local sale state with
 * the response rather than patching it locally, so tax/discount/total recalculation always reflects
 * server truth (plan §5, reconciliation note 7).
 */
@Injectable({ providedIn: 'root' })
export class SaleService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/sales`;

  create(payload: SaleCreateRequest): Observable<SaleResponse> {
    return this.http.post<ApiResponse<SaleResponse>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  list(filter: SaleListFilter = {}, page = 0, size = 10): Observable<PageResponse<SaleResponse>> {
    const params: Record<string, string | number> = { page, size };
    if (filter.status) {
      params['status'] = filter.status;
    }
    if (filter.storeId != null) {
      params['storeId'] = filter.storeId;
    }
    if (filter.terminalId != null) {
      params['terminalId'] = filter.terminalId;
    }
    return this.http
      .get<ApiResponse<PageResponse<SaleResponse>>>(this.baseUrl, { params })
      .pipe(map((res) => res.data));
  }

  getById(id: number): Observable<SaleResponse> {
    return this.http.get<ApiResponse<SaleResponse>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  addLine(saleId: number, payload: SaleLineAddRequest): Observable<SaleResponse> {
    return this.http
      .post<ApiResponse<SaleResponse>>(`${this.baseUrl}/${saleId}/lines`, payload)
      .pipe(map((res) => res.data));
  }

  updateLine(saleId: number, lineId: number, payload: SaleLineUpdateRequest): Observable<SaleResponse> {
    return this.http
      .put<ApiResponse<SaleResponse>>(`${this.baseUrl}/${saleId}/lines/${lineId}`, payload)
      .pipe(map((res) => res.data));
  }

  removeLine(saleId: number, lineId: number): Observable<SaleResponse> {
    return this.http
      .delete<ApiResponse<SaleResponse>>(`${this.baseUrl}/${saleId}/lines/${lineId}`)
      .pipe(map((res) => res.data));
  }

  updateDiscount(saleId: number, payload: SaleDiscountRequest): Observable<SaleResponse> {
    return this.http
      .put<ApiResponse<SaleResponse>>(`${this.baseUrl}/${saleId}/discount`, payload)
      .pipe(map((res) => res.data));
  }

  hold(saleId: number): Observable<SaleResponse> {
    return this.http.post<ApiResponse<SaleResponse>>(`${this.baseUrl}/${saleId}/hold`, {}).pipe(map((res) => res.data));
  }

  resume(saleId: number): Observable<SaleResponse> {
    return this.http
      .post<ApiResponse<SaleResponse>>(`${this.baseUrl}/${saleId}/resume`, {})
      .pipe(map((res) => res.data));
  }

  /** Safe to retry - a `COMPLETED` sale returns its existing result unchanged (plan reconciliation
   *  note 5). The payment dialog relies on this for its failure-recovery flow. */
  complete(saleId: number): Observable<CompleteSaleResponse> {
    return this.http
      .post<ApiResponse<CompleteSaleResponse>>(`${this.baseUrl}/${saleId}/complete`, {})
      .pipe(map((res) => res.data));
  }

  addPayment(saleId: number, payload: PaymentCreateRequest): Observable<PaymentResponse> {
    return this.http
      .post<ApiResponse<PaymentResponse>>(`${this.baseUrl}/${saleId}/payments`, payload)
      .pipe(map((res) => res.data));
  }

  listPayments(saleId: number): Observable<PaymentResponse[]> {
    return this.http
      .get<ApiResponse<PaymentResponse[]>>(`${this.baseUrl}/${saleId}/payments`)
      .pipe(map((res) => res.data));
  }

  /** Stub on the backend (logs only), real UI flow. Permission = same as viewing the sale. */
  sendNotification(saleId: number, payload: SaleNotificationRequest): Observable<void> {
    return this.http
      .post<ApiResponse<null>>(`${this.baseUrl}/${saleId}/notifications`, payload)
      .pipe(map(() => undefined));
  }
}
