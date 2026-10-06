import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { TaxGroup, TaxGroupCreateRequest, TaxGroupUpdateRequest } from '../models/tax-group.model';

@Injectable({ providedIn: 'root' })
export class TaxGroupService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/tax-groups`;

  list(page = 0, size = 10): Observable<PageResponse<TaxGroup>> {
    return this.http
      .get<ApiResponse<PageResponse<TaxGroup>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((res) => res.data));
  }

  /** Convenience for dropdowns elsewhere (Product's tax-group picker). */
  listAll(): Observable<TaxGroup[]> {
    return this.list(0, 500).pipe(map((page) => page.content));
  }

  getById(id: number): Observable<TaxGroup> {
    return this.http.get<ApiResponse<TaxGroup>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  create(payload: TaxGroupCreateRequest): Observable<TaxGroup> {
    return this.http.post<ApiResponse<TaxGroup>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  update(id: number, payload: TaxGroupUpdateRequest): Observable<TaxGroup> {
    return this.http.put<ApiResponse<TaxGroup>>(`${this.baseUrl}/${id}`, payload).pipe(map((res) => res.data));
  }
}
