import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/auth/auth.service';
import { MeView } from '../../../core/api/auth-api.service';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-me-test',
  standalone: true,
  imports: [CommonModule, ConfirmDialogComponent],
  templateUrl: './me-test.html',
  styleUrl: './me-test.scss',
})
export class MeTest {
  private readonly authService = inject(AuthService);

  readonly currentUser = this.authService.currentUser;
  readonly isAuthenticated = this.authService.isAuthenticated;

  isReloading = false;
  readonly logoutDialogOpen = signal(false);
  readonly loggingOut = signal(false);

  get userJson(): string {
    const user = this.currentUser();
    return user ? JSON.stringify(user, null, 2) : 'null';
  }

  get normalizedRole(): string {
    const role = this.currentUser()?.role;
    return role ? role.replace(/^ROLE_/, '') : '—';
  }

  reload(): void {
    this.isReloading = true;
    this.authService.loadMe().subscribe({
      next: () => {
        this.isReloading = false;
      },
      error: () => {
        this.isReloading = false;
      },
    });
  }

  logout(): void {
    this.logoutDialogOpen.set(true);
  }

  cancelLogout(): void {
    if (!this.loggingOut()) this.logoutDialogOpen.set(false);
  }

  confirmLogout(): void {
    if (this.loggingOut()) return;
    this.loggingOut.set(true);
    this.authService.logout().subscribe(() => {
      this.loggingOut.set(false);
      this.logoutDialogOpen.set(false);
    });
  }
}
