import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ApiResponse, unwrapApiResult } from '../models/api-response';

export interface Province {
  id: string;
  code: string;
  name: string;
}

export interface Commune {
  id: string;
  provinceId: string;
  code: string;
  name: string;
}

@Injectable({
  providedIn: 'root',
})
export class LocationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/location';

  getProvinces(): Observable<Province[]> {
    return this.http.get<ApiResponse<Province[]>>(`${this.baseUrl}/provinces`).pipe(
      map((res) => unwrapApiResult<Province[]>(res) ?? [])
    );
  }

  getCommunesByProvince(provinceId: string): Observable<Commune[]> {
    return this.http
      .get<ApiResponse<Commune[]>>(`${this.baseUrl}/provinces/${provinceId}/communes`)
      .pipe(map((res) => unwrapApiResult<Commune[]>(res) ?? []));
  }
}
