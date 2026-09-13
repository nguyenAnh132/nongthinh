import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response';

export interface FarmerProfileResponse {
  id: string;
  userId: string;
  firstName: string | null;
  lastName: string | null;
  gender: string | null;
  phone: string | null;
  provinceId: string | null;
  provinceName: string | null;
  communeId: string | null;
  communeName: string | null;
  addressDetail: string | null;
  avatarUrl: string | null;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface BrandProfileResponse {
  id: string;
  userId: string;
  companyName: string | null;
  taxCode: string | null;
  website: string | null;
  hotline: string | null;
  address: string | null;
  contactEmail: string | null;
  contactPersonName: string | null;
  logoUrl: string | null;
  businessLicenseUrl: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminProfileResponse {
  id: string;
  userId: string;
  firstName: string | null;
  lastName: string | null;
  phoneNumber: string | null;
  employeeCode: string | null;
  department: string | null;
  avatarUrl: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface MyProfilesResponse {
  farmer: FarmerProfileResponse | null;
  brand: BrandProfileResponse | null;
  admin: AdminProfileResponse | null;
}

export interface FarmerProfilePublicResponse {
  id: string;
  firstName: string;
  lastName: string;
  gender: string;
  provinceId: string | null;
  communeId: string | null;
  avatarUrl: string | null;
}

export interface BrandProfilePublicResponse {
  id: string;
  brandName: string;
  description: string | null;
  officeProvinceId: string | null;
  officeCommuneId: string | null;
  phone: string | null;
  representativeName: string | null;
  logoUrl: string | null;
  bannerUrl: string | null;
  websiteUrl: string | null;
  verified: boolean;
}

export interface UpdateFarmerProfilePayload {
  firstName: string;
  lastName: string;
  gender: string;
  phone?: string | null;
  provinceId?: string | null;
  communeId?: string | null;
  addressDetail?: string | null;
  avatarUrl?: string | null;
}

export interface UpdateBrandProfilePayload {
  companyName?: string | null;
  taxCode?: string | null;
  website?: string | null;
  hotline?: string | null;
  address?: string | null;
  contactEmail?: string | null;
  contactPersonName?: string | null;
  logoUrl?: string | null;
  businessLicenseUrl?: string | null;
}

export interface UpdateAdminProfilePayload {
  firstName?: string | null;
  lastName?: string | null;
  phoneNumber?: string | null;
  employeeCode?: string | null;
  department?: string | null;
  avatarUrl?: string | null;
}

@Injectable({ providedIn: 'root' })
export class ProfileApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/profile';

  getMyProfiles(): Observable<ApiResponse<MyProfilesResponse>> {
    return this.http.get<ApiResponse<MyProfilesResponse>>(`${this.baseUrl}/profiles/me`);
  }

  getMyFarmerProfile(): Observable<ApiResponse<FarmerProfileResponse>> {
    return this.http.get<ApiResponse<FarmerProfileResponse>>(`${this.baseUrl}/farmer-profiles/me`);
  }

  updateMyFarmerProfile(
    payload: UpdateFarmerProfilePayload,
  ): Observable<ApiResponse<FarmerProfileResponse>> {
    return this.http.patch<ApiResponse<FarmerProfileResponse>>(
      `${this.baseUrl}/farmer-profiles/me`,
      payload,
    );
  }

  updateMyFarmerAvatar(avatarUrl: string): Observable<ApiResponse<FarmerProfileResponse>> {
    return this.http.patch<ApiResponse<FarmerProfileResponse>>(
      `${this.baseUrl}/farmer-profiles/me/avatar`,
      { url: avatarUrl },
    );
  }

  getMyBrandProfile(): Observable<ApiResponse<BrandProfileResponse>> {
    return this.http.get<ApiResponse<BrandProfileResponse>>(`${this.baseUrl}/brand-profiles/me`);
  }

  updateMyBrandProfile(
    payload: UpdateBrandProfilePayload,
  ): Observable<ApiResponse<BrandProfileResponse>> {
    return this.http.patch<ApiResponse<BrandProfileResponse>>(
      `${this.baseUrl}/brand-profiles/me`,
      payload,
    );
  }

  getMyAdminProfile(): Observable<ApiResponse<AdminProfileResponse>> {
    return this.http.get<ApiResponse<AdminProfileResponse>>(`${this.baseUrl}/admin-profiles/me`);
  }

  updateMyAdminProfile(
    payload: UpdateAdminProfilePayload,
  ): Observable<ApiResponse<AdminProfileResponse>> {
    return this.http.patch<ApiResponse<AdminProfileResponse>>(
      `${this.baseUrl}/admin-profiles/me`,
      payload,
    );
  }

  getPublicFarmerProfileByUserId(
    userId: string,
  ): Observable<ApiResponse<FarmerProfilePublicResponse>> {
    return this.http.get<ApiResponse<FarmerProfilePublicResponse>>(
      `${this.baseUrl}/farmer-profiles/users/${userId}`,
    );
  }

  getPublicBrandProfileByUserId(
    userId: string,
  ): Observable<ApiResponse<BrandProfilePublicResponse>> {
    return this.http.get<ApiResponse<BrandProfilePublicResponse>>(
      `${this.baseUrl}/brand-profiles/users/${userId}`,
    );
  }
}
