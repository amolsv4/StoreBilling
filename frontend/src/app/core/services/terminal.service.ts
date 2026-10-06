import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { Terminal, TerminalCreateRequest, TerminalUpdateRequest } from '../models/terminal.model';

@Injectable({ providedIn: 'root' })
export class TerminalService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/terminals`;

  list(page = 0, size = 10): Observable<PageResponse<Terminal>> {
    return this.http
      .get<ApiResponse<PageResponse<Terminal>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((res) => res.data));
  }

  /** Convenience for dropdowns elsewhere (Configuration TERMINAL scope picker/resolve panel). */
  listAll(): Observable<Terminal[]> {
    return this.list(0, 500).pipe(map((page) => page.content));
  }

  getById(id: number): Observable<Terminal> {
    return this.http.get<ApiResponse<Terminal>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  create(payload: TerminalCreateRequest): Observable<Terminal> {
    return this.http.post<ApiResponse<Terminal>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  update(id: number, payload: TerminalUpdateRequest): Observable<Terminal> {
    return this.http.put<ApiResponse<Terminal>>(`${this.baseUrl}/${id}`, payload).pipe(map((res) => res.data));
  }
}
