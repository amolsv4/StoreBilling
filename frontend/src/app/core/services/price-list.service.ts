import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { PriceList, PriceListCreateRequest, PriceListUpdateRequest } from '../models/price-list.model';

@Injectable({ providedIn: 'root' })
export class PriceListService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/price-lists`;

  list(page = 0, size = 10): Observable<PageResponse<PriceList>> {
    return this.http
      .get<ApiResponse<PageResponse<PriceList>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((res) => res.data));
  }

  /** Convenience for dropdowns elsewhere (Product Price's price-list picker). */
  listAll(): Observable<PriceList[]> {
    return this.list(0, 500).pipe(map((page) => page.content));
  }

  getById(id: number): Observable<PriceList> {
    return this.http.get<ApiResponse<PriceList>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  create(payload: PriceListCreateRequest): Observable<PriceList> {
    return this.http.post<ApiResponse<PriceList>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  update(id: number, payload: PriceListUpdateRequest): Observable<PriceList> {
    return this.http.put<ApiResponse<PriceList>>(`${this.baseUrl}/${id}`, payload).pipe(map((res) => res.data));
  }
}
