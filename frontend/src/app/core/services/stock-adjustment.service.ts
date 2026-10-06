import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { StockAdjustment, StockAdjustmentCreateRequest } from '../models/stock-adjustment.model';

/** Create-only, ordinary Phase 2 list+dialog pattern (single product pick, no lines, no update). */
@Injectable({ providedIn: 'root' })
export class StockAdjustmentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/stock-adjustments`;

  list(storeId?: number, page = 0, size = 10): Observable<PageResponse<StockAdjustment>> {
    const params: Record<string, string | number> = { page, size };
    if (storeId != null) {
      params['storeId'] = storeId;
    }
    return this.http
      .get<ApiResponse<PageResponse<StockAdjustment>>>(this.baseUrl, { params })
      .pipe(map((res) => res.data));
  }

  create(payload: StockAdjustmentCreateRequest): Observable<StockAdjustment> {
    return this.http.post<ApiResponse<StockAdjustment>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }
}
