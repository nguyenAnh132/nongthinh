import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response';

/**
 * UserView cho admin panel — danh sách / chi tiết user.
 * Đây KHÔNG phải MeView (endpoint /me cho user hiện tại).
 */
export interface UserView {
  id: string;
  email: string;
  status: string;
  emailVerified: boolean;
  roles?: string[];
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/auth/users';

  getUsers(): Observable<ApiResponse<UserView[]>> {
    return this.http.get<ApiResponse<UserView[]>>(this.baseUrl);
  }

  getUser(id: string): Observable<ApiResponse<UserView>> {
    return this.http.get<ApiResponse<UserView>>(`${this.baseUrl}/${id}`);
  }

  updateUserStatus(id: string, status: string): Observable<ApiResponse<UserView>> {
    return this.http.patch<ApiResponse<UserView>>(`${this.baseUrl}/${id}`, { status });
  }

  updateUserRoles(id: string, roles: string[]): Observable<ApiResponse<UserView>> {
    return this.http.patch<ApiResponse<UserView>>(`${this.baseUrl}/${id}/roles`, { roles });
  }
}
