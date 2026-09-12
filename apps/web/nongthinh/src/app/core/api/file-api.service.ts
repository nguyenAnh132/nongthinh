import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response';

export type FilePurpose =
  | 'AVATAR'
  | 'BRAND_LOGO'
  | 'BRAND_BANNER'
  | 'BUSINESS_LICENSE'
  | 'PRODUCT_IMAGE'
  | 'DISEASE_IMAGE'
  | 'DIAGNOSIS_IMAGE'
  | 'POST_IMAGE'
  | 'POST_VIDEO'
  | 'MODEL_ARTIFACT';

export interface FileView {
  id: string;
  ownerUserId: string;
  purpose: FilePurpose | string;
  originalFileName: string;
  contentType: string;
  sizeBytes: number;
  publicUrl: string | null;
  status: string;
  createdAt: string | null;
  updatedAt: string | null;
}

/**
 * Upload binary qua API Gateway → file-service.
 * Auth dựa trên cookie HttpOnly access_token (withCredentials từ auth interceptor).
 */
@Injectable({ providedIn: 'root' })
export class FileApiService {
  private readonly http = inject(HttpClient);
  /** Gateway: /api/v1/files/** → file-service (/files) */
  private readonly baseUrl = '/api/v1/files';

  upload(file: File, purpose: FilePurpose): Observable<ApiResponse<FileView>> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    formData.append('purpose', purpose);
    // Không set Content-Type — browser tự gắn multipart boundary.
    return this.http.post<ApiResponse<FileView>>(`${this.baseUrl}/upload`, formData);
  }

  listMine(purpose: FilePurpose): Observable<ApiResponse<FileView[]>> {
    const params = new HttpParams().set('purpose', purpose);
    return this.http.get<ApiResponse<FileView[]>>(`${this.baseUrl}/me`, { params });
  }

  getContent(fileId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${fileId}/content`, { responseType: 'blob' });
  }
}
