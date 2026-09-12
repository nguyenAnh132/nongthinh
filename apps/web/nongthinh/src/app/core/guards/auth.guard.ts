import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../auth/auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.ensureMeLoaded().pipe(
    map((ok) => {
      if (ok) {
        return true;
      }
      return router.createUrlTree(['/']);
    }),
  );
};
