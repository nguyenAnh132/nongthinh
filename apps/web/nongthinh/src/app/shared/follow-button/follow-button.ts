import { Component, DestroyRef, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FollowApiService } from '../../core/api/follow-api.service';
import { AuthService } from '../../core/auth/auth.service';
import { apiErrorMessage } from '../../core/models/api-response';
import { ToastService } from '../toast/toast.service';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-follow-button',
  standalone: true,
  imports: [ConfirmDialogComponent],
  template: `
    @if (userId() && userId() !== auth.currentUser()?.userId) {
      <button type="button" [class.following]="api.following()[userId()]"
        [disabled]="api.pending()[userId()]" [attr.aria-pressed]="!!api.following()[userId()]"
        (click)="click($event)">
        {{ api.pending()[userId()] ? 'Đang xử lý…' : api.following()[userId()] ? 'Đang theo dõi' : '+ Theo dõi' }}
      </button>
    }
    @if (confirming()) {
      <app-confirm-dialog title="Bỏ theo dõi?" message="Bạn sẽ không nhận thông báo khi người này đăng bài viết mới."
        confirmLabel="Bỏ theo dõi" [busy]="!!api.pending()[userId()]" [error]="error()"
        (confirmed)="change(false)" (dismissed)="confirming.set(false)" />
    }
  `,
  styles: [`
    :host { display: inline-block; }
    button { border: 0; border-radius: 8px; padding: 9px 14px; background: #16834a; color: white;
      font: inherit; font-size: 14px; font-weight: 600; cursor: pointer; white-space: nowrap; }
    button.following { background: #edf0f3; color: #344054; }
    button:disabled { opacity: .6; cursor: wait; }
    button:focus-visible { outline: 3px solid #8bc9a8; outline-offset: 2px; }
  `],
})
export class FollowButton {
  readonly userId = input.required<string>();
  readonly api = inject(FollowApiService);
  readonly auth = inject(AuthService);
  readonly confirming = signal(false);
  readonly error = signal('');
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  click(event: MouseEvent) {
    event.stopPropagation();
    this.error.set('');
    if (this.api.following()[this.userId()]) this.confirming.set(true);
    else this.change(true);
  }
  change(following: boolean) {
    if (this.api.pending()[this.userId()]) return;
    this.api.setFollowing(this.userId(), following).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => this.confirming.set(false),
      error: error => {
        const message = apiErrorMessage(error, 'Không thể cập nhật theo dõi. Vui lòng thử lại.');
        this.error.set(message);
        this.toast.error(message);
      },
    });
  }
}
