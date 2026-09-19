import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response';

// ---------------------------------------------------------------------------
// Types khớp backend auth-service DTO
// ---------------------------------------------------------------------------

export interface ProfileView {
  profileId: string;
  type: string;
  displayName: string;
  avatarUrl: string | null;
  bannerUrl: string | null;
  status: string;
  rejectionReason: string | null;
  scheduledDeletionAt: string | null;
}

export interface MeFlags {
  requiresProfileCompletion: boolean;
  brandRejected: boolean;
  canReRegisterAt: string | null;
}

export interface MeView {
  userId: string;
  email: string;
  role: string;               // "ROLE_FARMER" | "ROLE_BRAND" | "ROLE_ADMIN"
  adminGroup: string | null;  // "SUPER_ADMIN" | "OPERATION" | null
  permissions: string[];
  profile: ProfileView;
  flags: MeFlags;
}

export interface RegisterFarmerPayload {
  email: string;
  password: string;
  temporary: boolean;
  enabled: boolean;
  firstName: string;
  lastName: string;
  gender: string;             // "MALE" | "FEMALE" | "OTHER"
  phone: string;              // 10 chữ số
  provinceId?: string;
  communeId?: string;
  addressDetail?: string;
  avatarUrl?: string;
}

export interface RegisterBrandPayload {
  email: string;
  password: string;
  temporary: boolean;
  enabled: boolean;
  brandName: string;
  taxCode?: string;
  description?: string;
  phone: string;              // 10 chữ số
  officeProvinceId?: string;
  officeCommuneId?: string;
  officeAddressDetail?: string;
  representativeName: string;
  representativePhone: string;
  representativeEmail: string;
  logoUrl?: string;
  bannerUrl?: string;
  websiteUrl?: string;
}

export type RegistrationType = 'FARMER' | 'BRAND';

export interface RegistrationRequiredView {
  email: string;
  role: 'ROLE_ADMIN' | 'ROLE_FARMER' | 'ROLE_BRAND' | 'ROLE_BRAND_PENDING';
}

export interface CompleteRegistrationPayload {
  phone: string;
  firstName?: string;
  lastName?: string;
  gender?: string;
  brandName?: string;
  representativeName?: string;
  representativePhone?: string;
  representativeEmail?: string;
}

export interface CompleteRegistrationView {
  userId: string;
  refreshRequired: boolean;
}

// ---------------------------------------------------------------------------
// Auth-service thông qua gateway
// ---------------------------------------------------------------------------

export const AUTH_SERVICE_URL = '/api/v1/auth';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = AUTH_SERVICE_URL;

  // ── Session ──────────────────────────────────────────────────────────

  completeRegistration(payload: CompleteRegistrationPayload): Observable<ApiResponse<CompleteRegistrationView>> {
    return this.http.post<ApiResponse<CompleteRegistrationView>>(
      `${this.baseUrl}/me/complete-registration`, payload, { withCredentials: true },
    );
  }

  /** GET /me — lấy thông tin user hiện tại từ cookie JWT */
  me(): Observable<ApiResponse<MeView>> {
    return this.http.get<ApiResponse<MeView>>(
      `${this.baseUrl}/me`,
      { withCredentials: true },
    );
  }

  /** POST /refresh — làm mới access token bằng refresh_token cookie */
  refresh(): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(
      `${this.baseUrl}/refresh`,
      null,
      { withCredentials: true },
    );
  }

  /** GET /logout — xóa cookie access_token và refresh_token */
  logout(): Observable<ApiResponse<void>> {
    return this.http.get<ApiResponse<void>>(
      `${this.baseUrl}/logout`,
      { withCredentials: true },
    );
  }

  // ── OAuth2 login (full-page redirect, không qua HttpClient) ──────

  /** Redirect trình duyệt sang Keycloak login page */
  login(): void {
    window.location.href = `${this.baseUrl}/oauth2/authorization/keycloak`;
  }

  // ── Đăng ký ──────────────────────────────────────────────────────────

  /** POST /farmers — đăng ký nông dân (201) */
  registerFarmer(payload: RegisterFarmerPayload): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}/farmers`,
      payload,
      { withCredentials: true },
    );
  }

  /** POST /brands — đăng ký thương hiệu (201) */
  registerBrand(payload: RegisterBrandPayload): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}/brands`,
      payload,
      { withCredentials: true },
    );
  }

}
