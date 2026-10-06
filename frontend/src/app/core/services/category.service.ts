import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { Category, CategoryCreateRequest, CategoryUpdateRequest } from '../models/category.model';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/categories`;

  list(page = 0, size = 10): Observable<PageResponse<Category>> {
    return this.http
      .get<ApiResponse<PageResponse<Category>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((res) => res.data));
  }

  /** Convenience for dropdowns elsewhere (own parent-category picker, Product's category picker). */
  listAll(): Observable<Category[]> {
    return this.list(0, 500).pipe(map((page) => page.content));
  }

  getById(id: number): Observable<Category> {
    return this.http.get<ApiResponse<Category>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  create(payload: CategoryCreateRequest): Observable<Category> {
    return this.http.post<ApiResponse<Category>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  update(id: number, payload: CategoryUpdateRequest): Observable<Category> {
    return this.http.put<ApiResponse<Category>>(`${this.baseUrl}/${id}`, payload).pipe(map((res) => res.data));
  }
}
