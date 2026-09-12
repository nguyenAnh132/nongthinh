import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../auth/auth.service';

export const activeBrandGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.ensureMeLoaded().pipe(
    map((authenticated) => {
      return authenticated &&
        auth.hasRole('BRAND') &&
        auth.currentUser()?.profile?.status === 'ACTIVE'
        ? true
        : router.createUrlTree(['/app/profile']);
    }),
  );
};
