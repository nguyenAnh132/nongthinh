import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response';

export type DiagnosisStatus = 'DISEASED' | 'HEALTHY' | 'UNDETERMINED';

export interface DiagnosisRequest {
  cropTypeId: string;
  fileIds: string[];
}

export interface ModelVersionInfo {
  id: string;
  name: string;
  versionId: string;
  version: string;
}

export interface DiseaseReference {
  id: string;
  displayName: string;
}

export interface DiagnosisBoundingBox {
  x: number;
  y: number;
  width: number;
  height: number;
}

export interface DiagnosisDetection {
  classCode: string;
  displayName: string;
  classKind: 'DISEASE' | 'HEALTHY';
  confidence: number;
  boundingBox: DiagnosisBoundingBox;
  disease: DiseaseReference | null;
}

export interface DiagnosisFileSnapshot {
  id: string;
  originalFileName: string;
  contentType: string;
  sizeBytes: number;
  createdAt: string;
  updatedAt: string;
}

export interface DiagnosisImage {
  file: DiagnosisFileSnapshot;
  width: number;
  height: number;
  status: DiagnosisStatus;
  detections: DiagnosisDetection[];
}

export interface DiagnosisGroup {
  status: DiagnosisStatus;
  classCode: string;
  displayName: string;
  disease: DiseaseReference | null;
  fileIds: string[];
  detectionCount: number;
}

export interface DiagnosisResult {
  diagnosisId: string;
  cropTypeId: string;
  model: ModelVersionInfo;
  status: DiagnosisStatus;
  images: DiagnosisImage[];
  groups: DiagnosisGroup[];
  processingTimeMs: number;
}

export interface HistorySummary {
  id: string;
  cropTypeId: string;
  modelId: string;
  modelVersionId: string;
  modelVersion: string;
  status: DiagnosisStatus;
  createdAt: string;
}

export interface DiagnosisHistoryDetail {
  history: HistorySummary;
  snapshot: DiagnosisResult;
}

@Injectable({ providedIn: 'root' })
export class DiagnosisApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/diagnoses';

  create(request: DiagnosisRequest): Observable<ApiResponse<DiagnosisResult>> {
    return this.http.post<ApiResponse<DiagnosisResult>>(this.baseUrl, request);
  }

  listHistory(): Observable<ApiResponse<HistorySummary[]>> {
    return this.http.get<ApiResponse<HistorySummary[]>>(`${this.baseUrl}/history`);
  }

  getHistory(diagnosisId: string): Observable<ApiResponse<DiagnosisHistoryDetail>> {
    return this.http.get<ApiResponse<DiagnosisHistoryDetail>>(
      `${this.baseUrl}/history/${diagnosisId}`,
    );
  }
}
