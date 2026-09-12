import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response';

export type SystemParamDataType = 'STRING' | 'INTEGER' | 'BOOLEAN';

export interface SystemParamView {
  id: number;
  name: string;
  value: string;
  description: string | null;
  dataType: SystemParamDataType;
  systemDefined: boolean;
  typeId: number | null;
  typeName: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface SystemParamTypeView {
  id: number;
  name: string;
  description: string | null;
  systemDefined: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface SystemParamTypeGroupView {
  id: number;
  name: string;
  description: string | null;
  systemDefined: boolean;
  createdAt: string;
  updatedAt: string;
  params: SystemParamView[];
}

export interface CreateSystemParamPayload {
  name: string;
  value: string;
  description?: string | null;
  dataType: SystemParamDataType;
  typeId?: number;
}

export interface UpdateSystemParamPayload {
  value: string;
  description?: string | null;
}

export interface AssignSystemParamTypePayload {
  typeId: number;
}

export interface CreateSystemParamTypePayload {
  name: string;
  description?: string | null;
}

@Injectable({ providedIn: 'root' })
export class SystemParamApiService {
  private readonly http = inject(HttpClient);
  private readonly paramsBaseUrl = '/api/v1/bo-portal/system-params';
  private readonly typesBaseUrl = '/api/v1/bo-portal/system-param-types';

  getSystemParams(): Observable<ApiResponse<SystemParamView[]>> {
    return this.http.get<ApiResponse<SystemParamView[]>>(this.paramsBaseUrl);
  }

  getGroupedSystemParamTypes(): Observable<ApiResponse<SystemParamTypeGroupView[]>> {
    return this.http.get<ApiResponse<SystemParamTypeGroupView[]>>(`${this.typesBaseUrl}/grouped`);
  }

  getSystemParamTypes(): Observable<ApiResponse<SystemParamTypeView[]>> {
    return this.http.get<ApiResponse<SystemParamTypeView[]>>(this.typesBaseUrl);
  }

  getSystemParamType(id: number): Observable<ApiResponse<SystemParamTypeView>> {
    return this.http.get<ApiResponse<SystemParamTypeView>>(`${this.typesBaseUrl}/${id}`);
  }

  createSystemParamType(
    payload: CreateSystemParamTypePayload,
  ): Observable<ApiResponse<SystemParamTypeView>> {
    return this.http.post<ApiResponse<SystemParamTypeView>>(this.typesBaseUrl, payload);
  }

  getSystemParam(name: string): Observable<ApiResponse<SystemParamView>> {
    return this.http.get<ApiResponse<SystemParamView>>(`${this.paramsBaseUrl}/${name}`);
  }

  createSystemParam(payload: CreateSystemParamPayload): Observable<ApiResponse<SystemParamView>> {
    return this.http.post<ApiResponse<SystemParamView>>(this.paramsBaseUrl, payload);
  }

  updateSystemParam(
    name: string,
    payload: UpdateSystemParamPayload,
  ): Observable<ApiResponse<SystemParamView>> {
    return this.http.put<ApiResponse<SystemParamView>>(`${this.paramsBaseUrl}/${name}`, payload);
  }

  assignSystemParamType(
    name: string,
    payload: AssignSystemParamTypePayload,
  ): Observable<ApiResponse<SystemParamView>> {
    return this.http.put<ApiResponse<SystemParamView>>(
      `${this.paramsBaseUrl}/${name}/type`,
      payload,
    );
  }

  deleteSystemParam(name: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.paramsBaseUrl}/${name}`);
  }
}
