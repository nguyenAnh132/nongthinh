import { Component, ElementRef, effect, inject, input, signal, untracked, viewChild } from '@angular/core';
import { Subscription } from 'rxjs';
import { RouterLink } from '@angular/router';
import { FollowApiService, FollowPage, FollowProfile } from '../../core/api/follow-api.service';
import { apiErrorMessage } from '../../core/models/api-response';
import { FollowButton } from '../follow-button/follow-button';
import { UserAvatarComponent } from '../user-avatar/user-avatar.component';

@Component({
  selector: 'app-follow-panel',
  standalone: true,
  imports: [RouterLink, FollowButton, UserAvatarComponent],
  templateUrl: './follow-panel.html',
  styleUrl: './follow-panel.scss',
})
export class FollowPanel {
  readonly userId = input.required<string>();
  readonly showIdentity = input(false);
  readonly profile = signal<FollowProfile | null>(null);
  readonly result = signal<FollowPage | null>(null);
  readonly kind = signal<'followers' | 'following'>('followers');
  readonly page = signal(0);
  readonly opened = signal(false);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly listError = signal('');
  readonly retry = signal(0);
  private readonly api = inject(FollowApiService);
  private readonly dialog = viewChild<ElementRef<HTMLDialogElement>>('dialog');
  private readonly members = viewChild<ElementRef<HTMLDivElement>>('members');
  private listRequest?: Subscription;
  private opener: HTMLElement | null = null;

  constructor() {
    effect(onCleanup => {
      const id = this.userId();
      this.api.revision();
      this.retry();
      if (!id) return;
      this.error.set('');
      const request = this.api.profile(id).subscribe({
        next: response => this.profile.set(response.result ?? null),
        error: error => {
          this.profile.set(null);
          this.error.set(apiErrorMessage(error, 'Không thể tải thông tin theo dõi.'));
        },
      });
      onCleanup(() => request.unsubscribe());
    });
    effect(onCleanup => {
      const id = this.userId();
      const kind = this.kind();
      this.api.revision();
      if (!this.opened() || !id) return;
      untracked(() => {
        this.result.set(null);
        const members = this.members()?.nativeElement;
        if (members) members.scrollTop = 0;
        this.loadPage(id, kind, 0);
      });
      onCleanup(() => this.listRequest?.unsubscribe());
    });
  }
  private loadPage(id: string, kind: 'followers' | 'following', page: number) {
    this.page.set(page);
    this.loading.set(true);
    this.listError.set('');
    this.listRequest = this.api.list(id, kind, page).subscribe({
      next: response => {
        const next = response.result;
        this.result.update(current => next ? {
          ...next,
          items: page === 0 ? next.items : Array.from(new Map(
            [...(current?.items ?? []), ...next.items].map(member => [member.userId, member]),
          ).values()),
        } : current ? { ...current, hasNext: false } : null);
        this.loading.set(false);
      },
      error: error => {
        this.loading.set(false);
        this.listError.set(apiErrorMessage(error, 'Không thể tải danh sách theo dõi.'));
      },
    });
  }
  onScroll(event: Event) {
    const element = event.currentTarget as HTMLElement;
    const scrollableHeight = element.scrollHeight - element.clientHeight;
    if (!this.opened() || this.loading() || this.listError() || !this.result()?.hasNext) return;
    if (scrollableHeight > 0 && element.scrollTop >= scrollableHeight * 0.7) {
      this.loadPage(this.userId(), this.kind(), this.page() + 1);
    }
  }
  retryList() {
    if (this.opened() && !this.loading()) this.loadPage(this.userId(), this.kind(), this.page());
  }
  open(kind: 'followers' | 'following', event: MouseEvent) {
    this.opener = event.currentTarget as HTMLElement;
    this.kind.set(kind);
    this.page.set(0);
    this.opened.set(true);
    this.dialog()?.nativeElement.showModal();
  }
  reload() { this.retry.update(value => value + 1); }
  close() {
    this.dialog()?.nativeElement.close();
    this.opened.set(false);
    this.opener?.focus();
  }
  backdrop(event: MouseEvent) {
    if (event.target === this.dialog()?.nativeElement) this.close();
  }
}
