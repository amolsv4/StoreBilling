import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { Organization, OrganizationCreateRequest, OrganizationUpdateRequest } from '../models/organization.model';

@Injectable({ providedIn: 'root' })
export class OrganizationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/organizations`;

  list(page = 0, size = 10): Observable<PageResponse<Organization>> {
    return this.http
      .get<ApiResponse<PageResponse<Organization>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((res) => res.data));
  }

  /** Convenience for dropdowns elsewhere (Store/User forms): fetches all active+inactive orgs. */
  listAll(): Observable<Organization[]> {
    return this.list(0, 200).pipe(map((page) => page.content));
  }

  getById(id: number): Observable<Organization> {
    return this.http.get<ApiResponse<Organization>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  create(payload: OrganizationCreateRequest): Observable<Organization> {
    return this.http.post<ApiResponse<Organization>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  update(id: number, payload: OrganizationUpdateRequest): Observable<Organization> {
    return this.http.put<ApiResponse<Organization>>(`${this.baseUrl}/${id}`, payload).pipe(map((res) => res.data));
  }
}
