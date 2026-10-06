import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { InventoryBalance, InventoryBalanceFilter, InventoryTransaction, InventoryTransactionFilter } from '../models/inventory.model';

/** Read-only: both endpoints here are plain `DataTableComponent` list screens with filter bars
 *  (plan §5) - no create/update/delete UI, matching the backend's read-only surface for this module. */
@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/inventory`;
  private readonly transactionsUrl = `${environment.apiBaseUrl}/inventory-transactions`;

  listBalances(filter: InventoryBalanceFilter = {}, page = 0, size = 10): Observable<PageResponse<InventoryBalance>> {
    const params: Record<string, string | number> = { page, size };
    if (filter.storeId != null) {
      params['storeId'] = filter.storeId;
    }
    if (filter.productId != null) {
      params['productId'] = filter.productId;
    }
    return this.http
      .get<ApiResponse<PageResponse<InventoryBalance>>>(this.baseUrl, { params })
      .pipe(map((res) => res.data));
  }

  listTransactions(
    filter: InventoryTransactionFilter = {},
    page = 0,
    size = 10
  ): Observable<PageResponse<InventoryTransaction>> {
    const params: Record<string, string | number> = { page, size };
    if (filter.storeId != null) {
      params['storeId'] = filter.storeId;
    }
    if (filter.productId != null) {
      params['productId'] = filter.productId;
    }
    if (filter.type) {
      params['type'] = filter.type;
    }
    return this.http
      .get<ApiResponse<PageResponse<InventoryTransaction>>>(this.transactionsUrl, { params })
      .pipe(map((res) => res.data));
  }
}
