import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { ProductPrice, ProductPriceCreateRequest, ProductPriceUpdateRequest } from '../models/product-price.model';

/** No `listAll()` - unlike Category/TaxComponent/TaxGroup/Product/PriceList, no other screen needs
 *  a full unpaged list of product prices. */
@Injectable({ providedIn: 'root' })
export class ProductPriceService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/product-prices`;

  list(page = 0, size = 10): Observable<PageResponse<ProductPrice>> {
    return this.http
      .get<ApiResponse<PageResponse<ProductPrice>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((res) => res.data));
  }

  getById(id: number): Observable<ProductPrice> {
    return this.http.get<ApiResponse<ProductPrice>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  create(payload: ProductPriceCreateRequest): Observable<ProductPrice> {
    return this.http.post<ApiResponse<ProductPrice>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  update(id: number, payload: ProductPriceUpdateRequest): Observable<ProductPrice> {
    return this.http.put<ApiResponse<ProductPrice>>(`${this.baseUrl}/${id}`, payload).pipe(map((res) => res.data));
  }
}
