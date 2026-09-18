import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, finalize, shareReplay, switchMap, tap, throwError } from 'rxjs';
import { AuthApiService, AUTH_SERVICE_URL } from '../api/auth-api.service';
import { ApiResponse } from '../models/api-response';
import { AuthService } from '../auth/auth.service';

/**
 * Public endpoints không cần refresh khi bị 401.
 * Tất cả dùng AUTH_SERVICE_URL prefix.
 */
function shouldSkipAuth(url: string): boolean {
  const base = AUTH_SERVICE_URL;
  return (
    url.startsWith(`${base}/farmers`) ||
    url.startsWith(`${base}/brands`) ||
    url.startsWith(`${base}/refresh`) ||
    url.startsWith(`${base}/logout`) ||
    url.includes('/oauth2/authorization') ||
    url.includes('/login/oauth2/code')
  );
}

function isAuthServiceRequest(url: string): boolean {
  return url.startsWith(AUTH_SERVICE_URL);
}

function isApiRequest(url: string): boolean {
  return url.startsWith('/api/v1/');
}

function attachAuthHeaders(url: string, req: Parameters<HttpInterceptorFn>[0]) {
  const headers: Record<string, string> = {};

  if (isAuthServiceRequest(url)) {
    headers['X-Client-Type'] = 'WEB';
  }

  return req.clone({
    setHeaders: headers,
    // Access/refresh tokens are HttpOnly cookies. The browser, not JavaScript,
    // is responsible for sending them to every API behind the gateway.
    withCredentials: isApiRequest(url),
  });
}

let refreshInFlight$: Observable<ApiResponse<void>> | null = null;
let refreshGeneration = 0;

function endSession(authService: AuthService, router: Router): void {
  authService.clearSession();
  if (router.url !== '/') {
    void router.navigateByUrl('/', { replaceUrl: true });
  }
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authApi = inject(AuthApiService);
  const authService = inject(AuthService);
  const router = inject(Router);
  const requestGeneration = refreshGeneration;

  const cloned = attachAuthHeaders(req.url, req);

  return next(cloned).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 403 && err.error?.code === 'BRAND_ACCESS_DENIED' && isApiRequest(req.url)) {
        void router.navigate(['/app/profile']);
        authService.loadMe().subscribe();
        return throwError(() => err);
      }
      if (err.status !== 401 || !isApiRequest(req.url) || shouldSkipAuth(req.url)) {
        return throwError(() => err);
      }

      const retryRequestOnce = () =>
        // Calling next directly bypasses this interceptor, so the original
        // request is retried at most once and cannot start a refresh loop.
        next(attachAuthHeaders(req.url, req)).pipe(
          catchError((retryError: HttpErrorResponse) => {
            if (retryError.status === 401) {
              endSession(authService, router);
            }
            return throwError(() => retryError);
          }),
        );

      // Another request may have refreshed while this older request was still
      // in flight. Retry with the new cookies instead of rotating the refresh
      // token a second time.
      if (requestGeneration < refreshGeneration) {
        return retryRequestOnce();
      }

      if (!refreshInFlight$) {
        refreshInFlight$ = authApi.refresh().pipe(
          tap(() => {
            refreshGeneration += 1;
          }),
          finalize(() => {
            refreshInFlight$ = null;
          }),
          shareReplay({ bufferSize: 1, refCount: false }),
        );
      }

      return refreshInFlight$.pipe(
        // This catch only handles POST /refresh. Keeping it before switchMap
        // prevents a 403/500 from the retried business request from logging out.
        catchError((refreshError: unknown) => {
          endSession(authService, router);
          return throwError(() => refreshError);
        }),
        switchMap(() => retryRequestOnce()),
      );
    }),
  );
};
