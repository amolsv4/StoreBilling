import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { BusinessType } from '../models/business-type.model';

/** Read-only reference data - no create/edit/deactivate in this phase. */
@Injectable({ providedIn: 'root' })
export class BusinessTypeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/business-types`;

  list(page = 0, size = 100): Observable<PageResponse<BusinessType>> {
    return this.http
      .get<ApiResponse<PageResponse<BusinessType>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((res) => res.data));
  }

  /** Convenience for dropdowns: fetches a large-enough single page and returns just the rows. */
  listAll(): Observable<BusinessType[]> {
    return this.list(0, 200).pipe(map((page) => page.content));
  }
}
