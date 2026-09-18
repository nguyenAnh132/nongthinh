import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { FilePurpose } from './file-api.service';
import { ApiResponse } from '../models/api-response';

export const FILE_PURPOSE_LABELS: Record<FilePurpose, string> = {
  AVATAR: 'Ảnh đại diện', BRAND_LOGO: 'Logo thương hiệu', BRAND_BANNER: 'Ảnh bìa thương hiệu',
  BUSINESS_LICENSE: 'Giấy phép kinh doanh', PRODUCT_IMAGE: 'Ảnh sản phẩm',
  DISEASE_IMAGE: 'Ảnh bệnh', DIAGNOSIS_IMAGE: 'Ảnh chẩn đoán',
  POST_IMAGE: 'Ảnh bài viết', POST_VIDEO: 'Video bài viết', MODEL_ARTIFACT: 'Model AI',
};
export const FILE_PURPOSES = Object.keys(FILE_PURPOSE_LABELS) as FilePurpose[];

export interface UploadPolicy {
  purpose: FilePurpose;
  maxSizeBytes: number | null;
  allowedContentTypes: string[] | null;
  allowedExtensions: string[] | null;
}
export interface FileTypeConfiguration {
  code: string;
  contentType: string;
  extension: string;
  enabled: boolean;
  updatedAt: string;
}
export interface FileUploadConfiguration {
  purpose: FilePurpose;
  maxSizeBytes: number | null;
  allowedContentTypes: string[] | null;
  fileTypes: FileTypeConfiguration[];
}

@Injectable({ providedIn: 'root' })
export class FileConfigurationApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/bo-portal';

  getUploadPolicy(purpose: FilePurpose) {
    return this.http.get<ApiResponse<UploadPolicy>>(`${this.baseUrl}/upload-policies/${purpose}`);
  }

  getConfiguration(purpose: FilePurpose) {
    return this.http.get<ApiResponse<FileUploadConfiguration>>(`${this.baseUrl}/file-upload-policies/${purpose}`);
  }

  updateFileType(purpose: FilePurpose, code: string, enabled: boolean) {
    return this.http.put<ApiResponse<FileTypeConfiguration>>(
      `${this.baseUrl}/file-upload-policies/${purpose}/file-types/${encodeURIComponent(code)}`, { enabled },
    );
  }
}
