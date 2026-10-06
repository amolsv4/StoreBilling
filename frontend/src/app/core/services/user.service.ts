import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { AppUser, UserCreateRequest, UserUpdateRequest } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/users`;

  list(page = 0, size = 10): Observable<PageResponse<AppUser>> {
    return this.http
      .get<ApiResponse<PageResponse<AppUser>>>(this.baseUrl, { params: { page, size } })
      .pipe(map((res) => res.data));
  }

  getById(id: number): Observable<AppUser> {
    return this.http.get<ApiResponse<AppUser>>(`${this.baseUrl}/${id}`).pipe(map((res) => res.data));
  }

  create(payload: UserCreateRequest): Observable<AppUser> {
    return this.http.post<ApiResponse<AppUser>>(this.baseUrl, payload).pipe(map((res) => res.data));
  }

  update(id: number, payload: UserUpdateRequest): Observable<AppUser> {
    return this.http.put<ApiResponse<AppUser>>(`${this.baseUrl}/${id}`, payload).pipe(map((res) => res.data));
  }
}
