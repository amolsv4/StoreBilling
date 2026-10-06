import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { Store, StoreCreateRequest, StoreUpdateRequest } from '../models/store.model';

@Injectable({ providedIn: 'root' })
export class StoreService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/stores`;

  list(page = 0, size = 10): Observable<PageResponse<Store>> {
    return this.http
      .get<ApiResponse<PageResponse<Store>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((res) => res.data));
  }

  /** Convenience for dropdowns elsewhere (Terminal/User forms, Configuration STORE scope picker). */
  listAll(): Observable<Store[]> {
    return this.list(0, 500).pipe(map((page) => page.content));
  }

  getById(id: number): Observable<Store> {
    return this.http.get<ApiResponse<Store>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  create(payload: StoreCreateRequest): Observable<Store> {
    return this.http.post<ApiResponse<Store>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  update(id: number, payload: StoreUpdateRequest): Observable<Store> {
    return this.http.put<ApiResponse<Store>>(`${this.baseUrl}/${id}`, payload).pipe(map((res) => res.data));
  }
}
