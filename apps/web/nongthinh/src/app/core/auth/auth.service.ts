import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { Observable, catchError, finalize, map, of, shareReplay, switchMap, tap } from 'rxjs';
import { AuthApiService, MeView } from '../api/auth-api.service';
import { RegistrationRequiredView } from '../api/auth-api.service';
import { HttpErrorResponse } from '@angular/common/http';
import { apiErrorMessage } from '../models/api-response';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authApi = inject(AuthApiService);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);

  private readonly currentUserSignal = signal<MeView | null>(null);
  private readonly registrationSignal = signal<RegistrationRequiredView | null>(null);
  private readonly sessionErrorSignal = signal<string | null>(null);
  readonly registrationRequired = this.registrationSignal.asReadonly();
  readonly sessionError = this.sessionErrorSignal.asReadonly();
  private meLoaded = false;
  private meInFlight$: Observable<MeView | null> | null = null;

  private explicitlyLoggedOut = false;

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

  // ── Role helpers ─────────────────────────────────────────────────────

  private normalizeRole(role: string): string {
    return role.replace(/^ROLE_/, '');
  }

  /** Backend trả `role` là string đơn ("ROLE_FARMER"). */
  hasRole(role: string): boolean {
    const want = this.normalizeRole(role);
    const current = this.currentUserSignal()?.role;
    if (!current) return false;
    return this.normalizeRole(current) === want;
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.some((r) => this.hasRole(r));
  }

  hasPermission(permission: string): boolean {
    const permissions = this.currentUserSignal()?.permissions ?? [];
    return permissions.includes(permission);
  }

  hasAnyPermission(permissions: string[]): boolean {
    return permissions.some((p) => this.hasPermission(p));
  }

  // ── Load /me ─────────────────────────────────────────────────────────

  loadMe(): Observable<MeView | null> {
    if (this.meInFlight$) {
      return this.meInFlight$;
    }
    this.meInFlight$ = this.authApi.me().pipe(
      switchMap((res) => {
        const user = res.result;
        const role = user?.role?.replace(/^ROLE_/, '');
        const active = user?.profile?.status === 'ACTIVE';
        if ((role === 'BRAND_PENDING' && active) || (role === 'BRAND' && !active)) {
          // /me reconciles Keycloak roles; obtain a token carrying the current role once.
          return this.authApi.refresh().pipe(switchMap(() => this.authApi.me()));
        }
        return of(res);
      }),
      map((res) => res.result ?? null),
      tap((user) => {
        this.currentUserSignal.set(user);
        this.registrationSignal.set(null);
        this.sessionErrorSignal.set(null);
        this.meLoaded = true;
      }),
      catchError((error: HttpErrorResponse) => {
        const registration = error.error?.result as RegistrationRequiredView | undefined;
        if (error.status === 409 && error.error?.code === 'AUTH_REGISTRATION_REQUIRED'
          && registration && typeof registration.email === 'string'
          && ['ROLE_ADMIN', 'ROLE_FARMER', 'ROLE_BRAND', 'ROLE_BRAND_PENDING'].includes(registration.role)) {
          this.currentUserSignal.set(null);
          this.registrationSignal.set(registration);
          this.sessionErrorSignal.set(null);
          this.meLoaded = true;
        } else if (error.status === 401) {
          this.clearSession();
        } else {
          // A downstream outage is not evidence that the user's session has ended.
          if (error.status === 403) {
            this.currentUserSignal.set(null);
            this.registrationSignal.set(null);
          }
          this.sessionErrorSignal.set(apiErrorMessage(error, 'Không tải được tài khoản. Vui lòng thử lại.'));
          this.meLoaded = false;
        }
        return of(null);
      }),
      finalize(() => {
        this.meInFlight$ = null;
      }),
      shareReplay({ bufferSize: 1, refCount: false }),
    );
    return this.meInFlight$;
  }

  ensureMeLoaded(): Observable<boolean> {
    if (this.explicitlyLoggedOut) {
      return of(false);
    }
    if (this.meLoaded) {
      return of(this.currentUserSignal() !== null);
    }
    return this.loadMe().pipe(map((u) => !!u));
  }

  // ── Session lifecycle ────────────────────────────────────────────────

  /**
   * Gọi khi app boot (provideAppInitializer).
   * Chỉ chạy trên browser — SSR skip.
   * GET /me → 200: lưu user
   *         → 401: interceptor thử POST /refresh → retry /me
   *         → refresh fail: clearSession, user = null (không redirect)
   */
  initializeSession(): Observable<void> {
    if (!isPlatformBrowser(this.platformId)) {
      return of(undefined);
    }
    return this.loadMe().pipe(map(() => undefined));
  }

  login(): void {
    this.explicitlyLoggedOut = false;
    this.meLoaded = false;
    this.currentUserSignal.set(null);
    this.registrationSignal.set(null);
    this.sessionErrorSignal.set(null);
    this.authApi.login();
  }

  logout(): Observable<void> {
    this.explicitlyLoggedOut = true;
    return this.authApi.logout().pipe(
      map(() => undefined),
      tap(() => {
        this.clearSession();
        void this.router.navigate(['/']);
      }),
      catchError(() => {
        this.clearSession();
        void this.router.navigate(['/']);
        return of(undefined);
      }),
    );
  }

  clearSession(): void {
    this.currentUserSignal.set(null);
    this.registrationSignal.set(null);
    this.sessionErrorSignal.set(null);
    this.meLoaded = true; // Set to true so we don't try to load again after logout
    this.meInFlight$ = null;
  }

  navigateAfterLogin(): void {
    if (this.registrationSignal()) {
      void this.router.navigate(['/complete-registration']);
      return;
    }
    const user = this.currentUserSignal();
    if (!user) {
      void this.router.navigate(['/login']);
      return;
    }
    if (this.hasRole('ADMIN')) {
      void this.router.navigate([this.adminHomePath()]);
      return;
    }
    if (this.hasRole('FARMER')) {
      void this.router.navigate(['/app/community']);
      return;
    }
    if (this.hasAnyRole(['BRAND', 'BRAND_PENDING'])) {
      void this.router.navigate(['/app/profile']);
      return;
    }
    void this.router.navigate(['/']);
  }

  adminHomePath(): string {
    if (this.hasRole('ADMIN')) {
      return '/admin/dashboard';
    }
    if (this.hasAnyPermission(['system:config:read', 'system:config:write'])) {
      return '/admin/params';
    }
    if (this.hasPermission('notification:email:manage')) {
      return '/admin/email-config';
    }
    if (this.hasAnyPermission(['admin:user:read', 'admin:user:write', 'admin:role:manage'])) {
      return '/admin/users';
    }
    if (this.hasPermission('brand:approve')) {
      return '/admin/brands';
    }
    if (this.hasPermission('content:moderate')) {
      return '/admin/articles';
    }
    if (this.hasPermission('incident:manage')) {
      return '/admin/logs';
    }
    return '/admin/params';
  }
}
