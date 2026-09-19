import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { CompleteRegistration } from './complete-registration';
import { AuthService } from '../../core/auth/auth.service';
import { AUTH_SERVICE_URL } from '../../core/api/auth-api.service';

describe('CompleteRegistration', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [CompleteRegistration],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  }));

  for (const role of ['ROLE_ADMIN', 'ROLE_FARMER', 'ROLE_BRAND_PENDING']) {
    it(`completes ${role} using server identity, refreshes and reloads me`, () => {
      const fixture = TestBed.createComponent(CompleteRegistration);
      const auth = TestBed.inject(AuthService);
      const navigate = vi.spyOn(auth, 'navigateAfterLogin').mockImplementation(() => undefined);
      const http = TestBed.inject(HttpTestingController);
      fixture.detectChanges();
      http.expectOne(AUTH_SERVICE_URL + '/me').flush({ code: 'AUTH_REGISTRATION_REQUIRED', result: { email: 'user@example.com', role } }, { status: 409, statusText: 'Conflict' });
      fixture.componentInstance.form.patchValue({ firstName: 'A', lastName: 'B', phone: '0900000000',
        brandName: 'Brand', representativeName: 'Nguyen Van An', representativePhone: '0900000001', representativeEmail: 'rep@example.com' });
      fixture.componentInstance.submit();
      const complete = http.expectOne(AUTH_SERVICE_URL + '/me/complete-registration');
      expect(complete.request.method).toBe('POST');
      expect(complete.request.body.userId).toBeUndefined();
      expect(complete.request.body.role).toBeUndefined();
      expect(complete.request.body.adminGroup).toBeUndefined();
      expect(complete.request.body.email).toBeUndefined();
      complete.flush({ result: { userId: 'local-id', refreshRequired: true } });
      http.expectOne(AUTH_SERVICE_URL + '/refresh').flush({});
      http.expectOne(AUTH_SERVICE_URL + '/me').flush({ result: { role, profile: { status: role === 'ROLE_BRAND_PENDING' ? 'PENDING_APPROVAL' : 'ACTIVE' } } });
      expect(navigate).toHaveBeenCalledOnce();
      expect(auth.registrationRequired()).toBeNull();
      expect(fixture.componentInstance.busy()).toBe(false);
      http.verify();
    });
  }

  it('does not expose the creation form when me failed due to infrastructure', () => {
    const fixture = TestBed.createComponent(CompleteRegistration);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(AUTH_SERVICE_URL + '/me').flush({}, { status: 503, statusText: 'Unavailable' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('form')).toBeNull();
    fixture.componentInstance.submit();
    http.expectNone(AUTH_SERVICE_URL + '/me/complete-registration');
    http.verify();
  });

  it('keeps form data after a failed request so the same operation can be retried', () => {
    const fixture = TestBed.createComponent(CompleteRegistration);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(AUTH_SERVICE_URL + '/me').flush({ code: 'AUTH_REGISTRATION_REQUIRED', result: { email: 'admin@example.com', role: 'ROLE_ADMIN' } }, { status: 409, statusText: 'Conflict' });
    fixture.componentInstance.form.patchValue({ firstName: 'An', lastName: 'Nguyen Van', phone: '0900000000' });
    fixture.componentInstance.submit();
    http.expectOne(AUTH_SERVICE_URL + '/me/complete-registration').flush({}, { status: 500, statusText: 'Unavailable' });
    expect(fixture.componentInstance.error()).toBeTruthy();
    expect(fixture.componentInstance.form.controls.firstName.value).toBe('An');
    expect(fixture.componentInstance.busy()).toBe(false);
    http.expectNone(AUTH_SERVICE_URL + '/refresh');
    http.verify();
  });
});
