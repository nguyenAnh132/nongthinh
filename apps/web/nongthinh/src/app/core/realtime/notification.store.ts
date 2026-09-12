import { DestroyRef, Injectable, computed, effect, inject, signal, untracked } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, Subscription, auditTime, map, switchMap } from 'rxjs';
import { NotificationApiService } from '../api/notification-api.service';
import { apiErrorMessage } from '../models/api-response';
import { InAppNotification, NotificationState } from './realtime.models';
import { RealtimeService } from './realtime.service';

@Injectable({ providedIn: 'root' })
export class NotificationStore {
  private readonly destroyRef = inject(DestroyRef);
  private readonly api = inject(NotificationApiService);
  private readonly realtime = inject(RealtimeService);
  private readonly refreshRequests = new Subject<void>();
  private readonly session = signal<string | null>(null);
  private request?: Subscription;
  private mutations = new Subscription();
  private generation = 0;
  private page = -1;
  private version = -1;
  readonly items = signal<InAppNotification[]>([]);
  readonly unreadCount = signal(0);
  readonly loading = signal(false);
  readonly hasMore = signal(false);
  readonly error = signal('');
  readonly enabled = computed(() => this.realtime.features().notifications);
  readonly configState = this.realtime.configState;
  readonly connectionState = this.realtime.connectionState;
  readonly lastEventId = this.realtime.lastEventId;
  readonly badge = computed(() => this.unreadCount() > 99 ? '99+' : String(this.unreadCount()));

  constructor() {
    this.refreshRequests.pipe(auditTime(100), takeUntilDestroyed()).subscribe(() => this.load(false));
    this.realtime.connected.pipe(takeUntilDestroyed()).subscribe(() => this.refresh());
    this.realtime.events.pipe(takeUntilDestroyed()).subscribe(event => {
      if (event.eventType.startsWith('notification.') && event.version > this.version) this.refresh();
    });
    effect(() => {
      const enabled = this.enabled();
      const session = this.session();
      if (enabled && session) untracked(() => this.refresh());
    });
    this.destroyRef.onDestroy(() => { this.request?.unsubscribe(); this.mutations.unsubscribe(); });
  }

  setSession(userId: string | null): void {
    if (this.session() === userId) return;
    this.generation++;
    this.request?.unsubscribe();
    this.mutations.unsubscribe();
    this.mutations = new Subscription();
    this.session.set(userId);
    this.items.set([]);
    this.unreadCount.set(0);
    this.loading.set(false);
    this.hasMore.set(false);
    this.error.set('');
    this.version = -1;
    this.page = -1;
  }

  refresh(): void { if (this.enabled() && this.session()) this.refreshRequests.next(); }
  retryConnection(): void { this.realtime.retryConfiguration(); }
  loadMore(): void { if (this.hasMore() && !this.loading()) this.load(true); }

  markRead(id: string): void {
    const generation = this.generation;
    this.mutations.add(this.api.markNotificationRead(id).subscribe({
      next: response => {
        if (generation !== this.generation) return;
        if (response.result) this.applyState(response.result);
        this.refresh();
      },
      error: error => { if (generation === this.generation) this.error.set(apiErrorMessage(error, 'Không thể đánh dấu đã đọc.')); },
    }));
  }

  markAllRead(): void {
    const generation = this.generation;
    this.mutations.add(this.api.markAllNotificationsRead().subscribe({
      next: response => {
        if (generation !== this.generation) return;
        if (response.result) this.applyState(response.result);
        this.refresh();
      },
      error: error => { if (generation === this.generation) this.error.set(apiErrorMessage(error, 'Không thể đánh dấu tất cả đã đọc.')); },
    }));
  }

  private load(append: boolean): void {
    if (!this.enabled() || !this.session()) return;
    this.request?.unsubscribe();
    const generation = this.generation;
    const page = append ? this.page + 1 : 0;
    this.loading.set(true);
    this.error.set('');
    // Read the revision first so a newer revision can never suppress refreshing an older list.
    this.request = this.api.getUnreadNotificationCount().pipe(
      switchMap(state => this.api.listNotifications(page).pipe(map(list => ({ list, state })))),
    ).subscribe({
      next: ({ list, state }) => {
        if (generation !== this.generation) return;
        if (list.result) {
          const items = append ? [...this.items(), ...list.result.items] : list.result.items;
          this.items.set([...new Map(items.map(item => [item.id, item])).values()]);
          this.page = page;
          this.hasMore.set(list.result.hasNext);
        }
        if (state.result) this.applyState(state.result);
        this.loading.set(false);
      },
      error: error => {
        if (generation !== this.generation) return;
        this.loading.set(false);
        this.error.set(apiErrorMessage(error, 'Không thể tải thông báo. Vui lòng thử lại.'));
      },
    });
  }

  private applyState(state: NotificationState): void {
    if (state.version < this.version) return;
    this.version = state.version;
    this.unreadCount.set(Math.max(0, state.unreadCount));
  }
}
