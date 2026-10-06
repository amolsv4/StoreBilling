import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { TaxComponent, TaxComponentCreateRequest, TaxComponentUpdateRequest } from '../models/tax-component.model';

@Injectable({ providedIn: 'root' })
export class TaxComponentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/tax-components`;

  list(page = 0, size = 10): Observable<PageResponse<TaxComponent>> {
    return this.http
      .get<ApiResponse<PageResponse<TaxComponent>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((res) => res.data));
  }

  /** Convenience for dropdowns elsewhere (Tax Group's component multi-select). */
  listAll(): Observable<TaxComponent[]> {
    return this.list(0, 500).pipe(map((page) => page.content));
  }

  getById(id: number): Observable<TaxComponent> {
    return this.http.get<ApiResponse<TaxComponent>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  create(payload: TaxComponentCreateRequest): Observable<TaxComponent> {
    return this.http.post<ApiResponse<TaxComponent>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  update(id: number, payload: TaxComponentUpdateRequest): Observable<TaxComponent> {
    return this.http.put<ApiResponse<TaxComponent>>(`${this.baseUrl}/${id}`, payload).pipe(map((res) => res.data));
  }
}
