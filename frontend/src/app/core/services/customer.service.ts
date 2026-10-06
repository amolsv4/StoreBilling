import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { Customer, CustomerCreateRequest, CustomerUpdateRequest } from '../models/customer.model';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/customers`;

  list(page = 0, size = 10): Observable<PageResponse<Customer>> {
    return this.http
      .get<ApiResponse<PageResponse<Customer>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((res) => res.data));
  }

  getById(id: number): Observable<Customer> {
    return this.http.get<ApiResponse<Customer>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  /** Mobile isn't unique, so this can return several customers (e.g. a shared family phone) - the
   *  caller (CustomerSearchComponent) lets the cashier pick among them. */
  lookupByMobile(organizationId: number, mobile: string): Observable<Customer[]> {
    return this.http
      .get<ApiResponse<Customer[]>>(`${this.baseUrl}/lookup`, { params: { organizationId, mobile } })
      .pipe(map((res) => res.data));
  }

  create(payload: CustomerCreateRequest): Observable<Customer> {
    return this.http.post<ApiResponse<Customer>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  update(id: number, payload: CustomerUpdateRequest): Observable<Customer> {
    return this.http.put<ApiResponse<Customer>>(`${this.baseUrl}/${id}`, payload).pipe(map((res) => res.data));
  }
}
