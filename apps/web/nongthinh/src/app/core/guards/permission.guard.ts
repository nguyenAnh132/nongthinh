import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../auth/auth.service';

export function permissionGuard(requiredPermissions: string[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    return auth.ensureMeLoaded().pipe(
      map((ok) => {
        if (auth.registrationRequired()) return router.createUrlTree(['/complete-registration']);
        if (!ok) {
          return router.createUrlTree(['/']);
        }
        if (auth.hasAnyPermission(requiredPermissions)) {
          return true;
        }
        if (auth.hasRole('ADMIN')) {
          return router.createUrlTree([auth.adminHomePath()]);
        }
        return router.createUrlTree(['/']);
      }),
    );
  };
}
