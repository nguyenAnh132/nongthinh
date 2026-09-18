import { canUseAppFeatures, isBrandAccount } from '../../core/auth/brand-access';
import { Component, DestroyRef, effect, inject, signal, untracked } from '@angular/core';
import { RealtimeService } from '../../core/realtime/realtime.service';
import { NotificationStore } from '../../core/realtime/notification.store';
import { NotificationPopover } from '../../shared/notification-popover/notification-popover';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/auth/auth.service';
import { UserAvatarComponent } from '../../shared/user-avatar/user-avatar.component';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog';
import {
  brandUserStatusLabel,
  canUploadBrandDocuments,
} from '../../views/user/brand-verification/brand-status.util';

@Component({
  selector: 'app-user-layout',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    UserAvatarComponent,
    ConfirmDialogComponent,
    NotificationPopover,
  ],
  templateUrl: './user-layout.html',
  styleUrl: './user-layout.scss',
})
export class UserLayout {
  private readonly realtime = inject(RealtimeService);
  private readonly notifications = inject(NotificationStore);
  private readonly destroyRef = inject(DestroyRef);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly currentUser = this.authService.currentUser;
  readonly logoutDialogOpen = signal(false);
  readonly loggingOut = signal(false);
  searchTerm = '';

  constructor() {
    effect(() => {
      const userId = canUseAppFeatures(this.currentUser()) ? this.currentUser()?.userId ?? null : null;
      untracked(() => {
        this.notifications.setSession(userId);
        if (userId) this.realtime.start(userId);
        else this.realtime.stop();
      });
    });
    this.destroyRef.onDestroy(() => {
      this.realtime.stop();
      this.notifications.setSession(null);
    });
  }

  displayName(): string {
    const user = this.currentUser();
    return user?.profile?.displayName?.trim() || user?.email?.split('@')[0] || 'Thành viên';
  }

  avatarUrl(): string | null {
    return this.currentUser()?.profile?.avatarUrl ?? null;
  }

  canUseDiagnosis(): boolean {
    return canUseAppFeatures(this.currentUser());
  }

  canUseCommunity(): boolean {
    return canUseAppFeatures(this.currentUser());
  }

  roleLabel(): string {
    const role = this.roleNormalized();
    if (role === 'BRAND' || role === 'BRAND_PENDING') return 'Thương hiệu';
    if (role === 'FARMER') return 'Nông dân';
    return 'Thành viên';
  }

  private roleNormalized(): string {
    const user = this.currentUser();
    if (!user) return '';
    return user.role.replace(/^ROLE_/, '');
  }

  isPending(): boolean {
    return this.currentUser()?.profile?.status === 'PENDING';
  }

  needsBrandAction(): boolean {
    const status = this.brandStatus();
    return !!status && canUploadBrandDocuments(status);
  }

  isBrandActive(): boolean {
    return this.roleNormalized() === 'BRAND' && this.brandStatus() === 'ACTIVE';
  }

  isBrand(): boolean {
    return isBrandAccount(this.currentUser());
  }

  brandBannerText(): string {
    const status = this.brandStatus();
    if (!status) return '';
    if (status === 'NEEDS_REVISION') {
      return 'Hồ sơ cần bổ sung. Nhấn vào avatar → Xem hồ sơ cá nhân để cập nhật thông tin và nộp lại giấy phép kinh doanh.';
    }
    return `Hồ sơ đang ở trạng thái «${brandUserStatusLabel(status)}». Nhấn vào avatar → Xem hồ sơ cá nhân để xác thực thông tin và nộp giấy phép kinh doanh.`;
  }

  private brandStatus(): string | null {
    if (!this.isBrand()) return null;
    return this.currentUser()?.profile?.status ?? null;
  }

  searchCommunity(): void {
    if (!this.canUseCommunity()) return;
    const query = this.searchTerm.trim();
    void this.router.navigate(['/app/community'], {
      queryParams: query ? { q: query } : {},
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
    this.realtime.stop();
    this.notifications.setSession(null);
    this.authService.logout().subscribe(() => {
      this.loggingOut.set(false);
      this.logoutDialogOpen.set(false);
    });
  }
}
