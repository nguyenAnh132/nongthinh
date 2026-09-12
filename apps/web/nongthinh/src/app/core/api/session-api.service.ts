import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response';

export interface SessionView {
  id: string;
  userId: string;
  revoked: boolean;
  revokedAt: string | null;
  revokedReason: string | null;
  accessExpiresAt: string;
  refreshExpiresAt: string;
  createdAt: string;
  lastSeenAt: string | null;
  userAgent: string | null;
  clientType: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class SessionApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/auth/sessions';

  getAllSessions(): Observable<ApiResponse<SessionView[]>> {
    return this.http.get<ApiResponse<SessionView[]>>(this.baseUrl);
  }

  revokeSession(id: string): Observable<ApiResponse<SessionView>> {
    return this.http.delete<ApiResponse<SessionView>>(`${this.baseUrl}/${id}`);
  }
}
