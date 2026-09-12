import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response';
import { BrandDocumentView, BrandProfileView } from './brand-profile-api.service';

export interface MyBrandProfileUpdatePayload {
  brandName: string;
  taxCode?: string | null;
  description?: string | null;
  phone: string;
  officeProvinceId?: string | null;
  officeCommuneId?: string | null;
  officeAddressDetail?: string | null;
  representativeName: string;
  representativePhone: string;
  representativeEmail: string;
  logoUrl?: string | null;
  bannerUrl?: string | null;
  websiteUrl?: string | null;
}

@Injectable({ providedIn: 'root' })
export class MyBrandProfileApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/profile/brand-profiles';

  getMe(): Observable<ApiResponse<BrandProfileView>> {
    return this.http.get<ApiResponse<BrandProfileView>>(`${this.baseUrl}/me`);
  }

  updateMe(payload: MyBrandProfileUpdatePayload): Observable<ApiResponse<BrandProfileView>> {
    return this.http.patch<ApiResponse<BrandProfileView>>(`${this.baseUrl}/me`, payload);
  }

  updateLogo(logoUrl: string): Observable<ApiResponse<BrandProfileView>> {
    return this.http.patch<ApiResponse<BrandProfileView>>(`${this.baseUrl}/me/logo`, {
      url: logoUrl,
    });
  }

  updateBanner(bannerUrl: string): Observable<ApiResponse<BrandProfileView>> {
    return this.http.patch<ApiResponse<BrandProfileView>>(`${this.baseUrl}/me/banner`, {
      url: bannerUrl,
    });
  }

  getMyDocument(): Observable<ApiResponse<BrandDocumentView | null>> {
    return this.http.get<ApiResponse<BrandDocumentView | null>>(`${this.baseUrl}/me/documents`);
  }

  uploadDocument(businessLicenseUrl: string): Observable<ApiResponse<BrandDocumentView>> {
    return this.http.post<ApiResponse<BrandDocumentView>>(`${this.baseUrl}/me/documents`, {
      businessLicenseUrl,
    });
  }

  submitDocuments(): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/me/documents-submitted`, {});
  }
}
