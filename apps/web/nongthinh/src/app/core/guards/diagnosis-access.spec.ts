import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, CanActivateFn, RouterStateSnapshot, provideRouter } from '@angular/router';
import { firstValueFrom, isObservable, of } from 'rxjs';
import { routes } from '../../app.routes';
import { AuthService } from '../auth/auth.service';

describe('Diagnosis route access', () => {
  for (const role of ['FARMER', 'BRAND', 'ADMIN', 'UNKNOWN']) {
    it(`checks access for ${role} using the configured route guard`, async () => {
      TestBed.configureTestingModule({ providers: [provideRouter([]), {
        provide: AuthService,
        useValue: {
          ensureMeLoaded: () => of(true),
          hasAnyRole: (allowed: string[]) => allowed.includes(role),
          hasRole: (expected: string) => expected === role,
          adminHomePath: () => '/admin/dashboard',
        },
      }] });
      const route = routes.find(item => item.path === 'app')!.children!.find(item => item.path === 'diagnosis')!;
      const guard = route.canActivate![0] as CanActivateFn;
      const result = TestBed.runInInjectionContext(() => guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot));
      const resolved = isObservable(result) ? await firstValueFrom(result) : await result;
      if (role === 'FARMER' || role === 'BRAND') expect(resolved).toBe(true);
      else expect(resolved).not.toBe(true);
    });
  }
});
