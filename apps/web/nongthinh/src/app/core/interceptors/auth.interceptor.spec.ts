import {
  HttpClient,
  HttpErrorResponse,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { AuthService } from '../auth/auth.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpTesting: HttpTestingController;
  let clearSession: ReturnType<typeof vi.fn>;
  let navigateByUrl: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    clearSession = vi.fn();
    navigateByUrl = vi.fn(() => Promise.resolve(true));

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        {
          provide: AuthService,
          useValue: { clearSession },
        },
        {
          provide: Router,
          useValue: {
            url: '/admin/dashboard',
            navigateByUrl,
          },
        },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('refreshes once and retries the failed API request', () => {
    let result: unknown;
    httpClient.get('/api/v1/admin/dashboard').subscribe((value) => {
      result = value;
    });

    const initial = httpTesting.expectOne('/api/v1/admin/dashboard');
    expect(initial.request.withCredentials).toBe(true);
    expect(initial.request.headers.has('Authorization')).toBe(false);
    initial.flush(null, { status: 401, statusText: 'Unauthorized' });

    const refresh = httpTesting.expectOne('/api/v1/auth/refresh');
    expect(refresh.request.method).toBe('POST');
    expect(refresh.request.withCredentials).toBe(true);
    expect(refresh.request.headers.get('X-Client-Type')).toBe('WEB');
    refresh.flush({ result: null });

    const retry = httpTesting.expectOne('/api/v1/admin/dashboard');
    expect(retry.request.withCredentials).toBe(true);
    retry.flush({ result: { total: 1 } });

    expect(result).toEqual({ result: { total: 1 } });
    expect(clearSession).not.toHaveBeenCalled();
    expect(navigateByUrl).not.toHaveBeenCalled();
  });

  it('shares one refresh request across concurrent 401 responses', () => {
    httpClient.get('/api/v1/admin/users').subscribe();
    httpClient.get('/api/v1/farmer/diagnoses').subscribe();

    httpTesting
      .expectOne('/api/v1/admin/users')
      .flush(null, { status: 401, statusText: 'Unauthorized' });
    httpTesting
      .expectOne('/api/v1/farmer/diagnoses')
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    const refresh = httpTesting.expectOne('/api/v1/auth/refresh');
    refresh.flush({ result: null });

    httpTesting.expectOne('/api/v1/admin/users').flush({ result: [] });
    httpTesting.expectOne('/api/v1/farmer/diagnoses').flush({ result: [] });
    httpTesting.expectNone('/api/v1/auth/refresh');
  });

  it('does not refresh again when a stale request returns 401 after refresh completed', () => {
    httpClient.get('/api/v1/admin/users').subscribe();
    httpClient.get('/api/v1/farmer/diagnoses').subscribe();

    const adminRequest = httpTesting.expectOne('/api/v1/admin/users');
    const delayedFarmerRequest = httpTesting.expectOne('/api/v1/farmer/diagnoses');

    adminRequest.flush(null, { status: 401, statusText: 'Unauthorized' });
    httpTesting.expectOne('/api/v1/auth/refresh').flush({ result: null });
    httpTesting.expectOne('/api/v1/admin/users').flush({ result: [] });

    delayedFarmerRequest.flush(null, { status: 401, statusText: 'Unauthorized' });

    httpTesting.expectNone('/api/v1/auth/refresh');
    httpTesting.expectOne('/api/v1/farmer/diagnoses').flush({ result: [] });
    expect(clearSession).not.toHaveBeenCalled();
  });

  it('ends the session and returns to landing when refresh fails', () => {
    let receivedError: HttpErrorResponse | undefined;
    httpClient.get('/api/v1/admin/dashboard').subscribe({
      error: (error: HttpErrorResponse) => {
        receivedError = error;
      },
    });

    httpTesting
      .expectOne('/api/v1/admin/dashboard')
      .flush(null, { status: 401, statusText: 'Unauthorized' });
    httpTesting
      .expectOne('/api/v1/auth/refresh')
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(receivedError?.status).toBe(401);
    expect(clearSession).toHaveBeenCalledOnce();
    expect(navigateByUrl).toHaveBeenCalledWith('/', { replaceUrl: true });
  });

  it('does not log out when the retried request fails with a non-auth error', () => {
    let receivedError: HttpErrorResponse | undefined;
    httpClient.get('/api/v1/farmer/diagnoses').subscribe({
      error: (error: HttpErrorResponse) => {
        receivedError = error;
      },
    });

    httpTesting
      .expectOne('/api/v1/farmer/diagnoses')
      .flush(null, { status: 401, statusText: 'Unauthorized' });
    httpTesting.expectOne('/api/v1/auth/refresh').flush({ result: null });
    httpTesting
      .expectOne('/api/v1/farmer/diagnoses')
      .flush(null, { status: 500, statusText: 'Server Error' });

    expect(receivedError?.status).toBe(500);
    expect(clearSession).not.toHaveBeenCalled();
    expect(navigateByUrl).not.toHaveBeenCalled();
  });

  it('ends the session when the retried request is still unauthorized', () => {
    httpClient.get('/api/v1/admin/dashboard').subscribe({ error: () => undefined });

    httpTesting
      .expectOne('/api/v1/admin/dashboard')
      .flush(null, { status: 401, statusText: 'Unauthorized' });
    httpTesting.expectOne('/api/v1/auth/refresh').flush({ result: null });
    httpTesting
      .expectOne('/api/v1/admin/dashboard')
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(clearSession).toHaveBeenCalledOnce();
    expect(navigateByUrl).toHaveBeenCalledWith('/', { replaceUrl: true });
    httpTesting.expectNone('/api/v1/auth/refresh');
  });
});
