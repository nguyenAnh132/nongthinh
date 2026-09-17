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
