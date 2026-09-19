import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { AUTH_SERVICE_URL } from '../api/auth-api.service';

describe('AuthService role changes', () => {
  for (const [before, status, after] of [
    ['ROLE_BRAND_PENDING', 'ACTIVE', 'ROLE_BRAND'],
    ['ROLE_BRAND', 'REJECTED', 'ROLE_BRAND_PENDING'],
  ]) {
    it(`refreshes the token once for ${status}`, () => {
      TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()] });
      const auth = TestBed.inject(AuthService);
      const http = TestBed.inject(HttpTestingController);
      const result = vi.fn();
      auth.loadMe().subscribe(result);
      http.expectOne(AUTH_SERVICE_URL + '/me').flush({ result: { role: before, profile: { status } } });
      http.expectOne(AUTH_SERVICE_URL + '/refresh').flush({ code: '1000' });
      http.expectOne(AUTH_SERVICE_URL + '/me').flush({ result: { role: after, profile: { status } } });
      expect(auth.currentUser()?.role).toBe(after);
      expect(result).toHaveBeenCalledOnce();
      http.verify();
    });
  }
});

describe('AuthService registration state', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()] });
  });

  it('recognizes only the dedicated 409 as incomplete registration', () => {
    const auth = TestBed.inject(AuthService);
    const http = TestBed.inject(HttpTestingController);
    auth.loadMe().subscribe();
    http.expectOne(AUTH_SERVICE_URL + '/me').flush({ code: 'AUTH_REGISTRATION_REQUIRED', result: { email: 'admin@example.com', role: 'ROLE_ADMIN' } }, { status: 409, statusText: 'Conflict' });
    expect(auth.registrationRequired()?.role).toBe('ROLE_ADMIN');
    expect(auth.currentUser()).toBeNull();
    expect(auth.sessionError()).toBeNull();
    http.verify();
  });

  it('does not interpret 404 or server failures as a request to create a profile', () => {
    const auth = TestBed.inject(AuthService);
    const http = TestBed.inject(HttpTestingController);
    for (const status of [404, 500, 503]) {
      auth.loadMe().subscribe();
      http.expectOne(AUTH_SERVICE_URL + '/me').flush({ code: 'OTHER_ERROR' }, { status, statusText: 'Unavailable' });
      expect(auth.registrationRequired()).toBeNull();
      expect(auth.sessionError()).toBeTruthy();
    }
    http.verify();
  });

  it('preserves a known session during a downstream outage and clears it on 401', () => {
    const auth = TestBed.inject(AuthService);
    const http = TestBed.inject(HttpTestingController);
    auth.loadMe().subscribe();
    http.expectOne(AUTH_SERVICE_URL + '/me').flush({ result: { role: 'ROLE_ADMIN', profile: { status: 'ACTIVE' } } });
    auth.loadMe().subscribe();
    http.expectOne(AUTH_SERVICE_URL + '/me').flush({}, { status: 500, statusText: 'Unavailable' });
    expect(auth.currentUser()?.role).toBe('ROLE_ADMIN');
    expect(auth.sessionError()).toBeTruthy();
    auth.loadMe().subscribe();
    http.expectOne(AUTH_SERVICE_URL + '/me').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(auth.currentUser()).toBeNull();
    expect(auth.registrationRequired()).toBeNull();
    http.verify();
  });
});
