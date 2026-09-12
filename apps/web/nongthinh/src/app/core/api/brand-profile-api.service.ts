import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response';

export interface BrandProfileView {
  id: string;
  userId: string;
  brandName: string;
  taxCode: string | null;
  description: string | null;
  phone: string | null;
  officeProvinceId: string | null;
  officeCommuneId: string | null;
  officeAddressDetail: string | null;
  representativeName: string | null;
  representativePhone: string | null;
  representativeEmail: string | null;
  logoUrl: string | null;
  bannerUrl: string | null;
  websiteUrl: string | null;
  status: string;
  rejectionReason: string | null;
  scheduledDeletionAt: string | null;
  rejectedAt: string | null;
  approvedAt: string | null;
  approvedBy: string | null;
  rejectedBy: string | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface BrandDocumentView {
  id: string;
  brandProfileId: string;
  businessLicenseUrl: string | null;
  reviewStatus: string;
  reviewedBy: string | null;
  reviewedAt: string | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface BrandVerificationLogView {
  id: string;
  brandProfileId: string;
  adminUserId: string;
  phoneCalled: string;
  result: string;
  note: string | null;
  verifiedAt: string | null;
  createdAt: string | null;
}

export interface BrandLifecycleLogView {
  id: string;
  brandProfileId: string;
  action: string;
  actorUserId: string | null;
  fromStatus: string | null;
  toStatus: string | null;
  payloadJson: string | null;
  createdAt: string | null;
}

export interface AdminBrandProfileDetailView {
  profile: BrandProfileView;
  documents: BrandDocumentView[];
  verificationLogs: BrandVerificationLogView[];
  lifecycleLogs: BrandLifecycleLogView[];
}

@Injectable({ providedIn: 'root' })
export class BrandProfileApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/profile/admin/brand-profiles';

  list(status?: string | null): Observable<ApiResponse<BrandProfileView[]>> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<ApiResponse<BrandProfileView[]>>(this.baseUrl, { params });
  }

  listPending(): Observable<ApiResponse<BrandProfileView[]>> {
    return this.http.get<ApiResponse<BrandProfileView[]>>(`${this.baseUrl}/pending`);
  }

  getDetail(id: string): Observable<ApiResponse<AdminBrandProfileDetailView>> {
    return this.http.get<ApiResponse<AdminBrandProfileDetailView>>(`${this.baseUrl}/${id}`);
  }

  rejectEarly(
    id: string,
    rejectionReason: string
  ): Observable<ApiResponse<BrandProfileView>> {
    return this.http.post<ApiResponse<BrandProfileView>>(
      `${this.baseUrl}/${id}/reject-early`,
      { rejectionReason }
    );
  }
}
