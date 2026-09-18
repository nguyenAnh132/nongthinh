import { inject } from '@angular/core';
import { CanActivateChildFn, Router } from '@angular/router';
import { map, of, switchMap } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { canUseAppFeatures, isBrandAccount } from '../auth/brand-access';

export const brandFeatureGuard: CanActivateChildFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.ensureMeLoaded().pipe(
    switchMap((authenticated) => {
      if (!authenticated) return of(router.createUrlTree(['/login']));
      if (!isBrandAccount(auth.currentUser()) || route.routeConfig?.path === 'profile') return of(true);
      // Approval can change while a tab is open.
      return auth.loadMe().pipe(map((user) => canUseAppFeatures(user)
        ? true : router.createUrlTree(['/app/profile'])));
    }),
  );
};
