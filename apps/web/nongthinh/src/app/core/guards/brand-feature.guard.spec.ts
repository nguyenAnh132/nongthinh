import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, provideRouter } from '@angular/router';
import { firstValueFrom, isObservable, of } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { brandFeatureGuard } from './brand-feature.guard';
import { routes } from '../../app.routes';

describe('Brand feature access', () => {
  for (const role of ['ROLE_BRAND', 'ROLE_BRAND_PENDING']) {
    for (const status of ['PENDING_APPROVAL', 'UNDER_REVIEW', 'NEEDS_REVISION', 'READY_FOR_FINAL_REVIEW',
      'REJECTED', 'LOCKED', 'DISABLED', 'DELETED', 'ACTIVE', undefined]) {
      it(`checks ${role} / ${status} before entering a feature`, async () => {
        TestBed.configureTestingModule({ providers: [provideRouter([]), {
          provide: AuthService,
          useValue: { ensureMeLoaded: () => of(true), currentUser: () => ({ role, profile: { status } }),
            loadMe: () => of({ role, profile: { status } }) },
        }] });
        const route = { routeConfig: { path: 'community' } } as ActivatedRouteSnapshot;
        const result = TestBed.runInInjectionContext(() => brandFeatureGuard(route, {} as RouterStateSnapshot));
        const resolved = isObservable(result) ? await firstValueFrom(result) : await result;
        if (role === 'ROLE_BRAND' && status === 'ACTIVE') expect(resolved).toBe(true);
        else expect(TestBed.inject(Router).serializeUrl(resolved as any)).toBe('/app/profile');
      });
    }
  }

  it('keeps the profile accessible with a pending role and enforces policy for all child routes', async () => {
    TestBed.configureTestingModule({ providers: [provideRouter([]), {
      provide: AuthService, useValue: {
        ensureMeLoaded: () => of(true), currentUser: () => ({ role: 'ROLE_BRAND_PENDING' }),
      },
    }] });
    const result = TestBed.runInInjectionContext(() => brandFeatureGuard(
      { routeConfig: { path: 'profile' } } as ActivatedRouteSnapshot, {} as RouterStateSnapshot));
    expect(isObservable(result) ? await firstValueFrom(result) : await result).toBe(true);
    expect(routes.find(route => route.path === 'app')?.canActivateChild).toContain(brandFeatureGuard);
  });
});
