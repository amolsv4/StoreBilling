import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { Product, ProductCreateRequest, ProductUpdateRequest } from '../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/products`;

  list(page = 0, size = 10): Observable<PageResponse<Product>> {
    return this.http
      .get<ApiResponse<PageResponse<Product>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((res) => res.data));
  }

  /**
   * Extends the existing list endpoint with an optional `search` param (LIKE across name/sku/
   * barcode, per plan reconciliation note 3) - used by `ProductSearchComponent`'s debounced
   * typeahead. Same `PRODUCT_VIEW` permission and envelope as the plain list.
   */
  search(organizationId: number, term: string, page = 0, size = 10): Observable<PageResponse<Product>> {
    return this.http
      .get<ApiResponse<PageResponse<Product>>>(this.baseUrl, {
        params: { organizationId, search: term, page, size }
      })
      .pipe(map((res) => res.data));
  }

  /**
   * Exact SKU/barcode match for the Enter-key fast path (barcode-scanner-style entry). Returns
   * `null` on a 404 (no match) instead of propagating - the caller falls back to the autocomplete
   * list rather than treating "no exact match" as an error.
   */
  lookup(organizationId: number, code: string): Observable<Product | null> {
    return this.http
      .get<ApiResponse<Product>>(`${this.baseUrl}/lookup`, { params: { organizationId, code } })
      .pipe(
        map((res) => res.data),
        catchError(() => of(null))
      );
  }

  /** Convenience for dropdowns elsewhere (Product Price's product picker). */
  listAll(): Observable<Product[]> {
    return this.list(0, 500).pipe(map((page) => page.content));
  }

  getById(id: number): Observable<Product> {
    return this.http.get<ApiResponse<Product>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  create(payload: ProductCreateRequest): Observable<Product> {
    return this.http.post<ApiResponse<Product>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  update(id: number, payload: ProductUpdateRequest): Observable<Product> {
    return this.http.put<ApiResponse<Product>>(`${this.baseUrl}/${id}`, payload).pipe(map((res) => res.data));
  }
}
