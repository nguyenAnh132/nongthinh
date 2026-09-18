import { canUseAppFeatures, isBrandAccount } from '../auth/brand-access';
import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Subject, Subscription } from 'rxjs';
import { ApiResponse } from '../models/api-response';
import { RealtimeEvent, RealtimeFeatures } from './realtime.models';
import { AuthApiService } from '../api/auth-api.service';

@Injectable({ providedIn: 'root' })
export class RealtimeService {
  private readonly http = inject(HttpClient);
  private readonly authApi = inject(AuthApiService);
  private readonly platform = inject(PLATFORM_ID);
  private source: EventSource | null = null;
  private session: string | null = null;
  private communityActive = false;
  private generation = 0;
  private retryTimer?: ReturnType<typeof setTimeout>;
  private configRequest?: Subscription;
  private sessionProbe?: Subscription;
  readonly features = signal<RealtimeFeatures>({ notifications: false, postEngagement: false });
  readonly configState = signal<'idle' | 'loading' | 'ready' | 'error'>('idle');
  readonly connectionState = signal<'closed' | 'connecting' | 'open' | 'reconnecting'>('closed');
  readonly lastEventId = signal('');
  readonly events = new Subject<RealtimeEvent>();
  readonly connected = new Subject<void>();

  start(userId: string): void {
    if (!isPlatformBrowser(this.platform) || this.session === userId) return;
    this.stop();
    this.session = userId;
    this.configState.set('loading');
    const generation = this.generation;
    this.configRequest = this.http.get<ApiResponse<RealtimeFeatures>>('/api/v1/notification/events/config')
      .subscribe({
        next: response => {
          if (generation !== this.generation) return;
          this.features.set(response.result ?? { notifications: false, postEngagement: false });
          this.configState.set('ready');
          this.connect();
        },
        error: () => {
          if (generation !== this.generation) return;
          this.configState.set('error');
          this.connectionState.set('reconnecting');
          this.retryTimer = setTimeout(() => {
            if (generation !== this.generation || !this.session) return;
            const userId = this.session;
            this.session = null;
            this.start(userId);
          }, 3000);
        },
      });
  }

  setCommunityActive(active: boolean): void {
    if (this.communityActive === active) return;
    this.communityActive = active;
    // Do not cancel configuration retries while changing routes before config is available.
    if (this.configState() === 'ready') this.connect();
  }

  retryConfiguration(): void {
    const userId = this.session;
    if (!userId) return;
    this.session = null;
    this.start(userId);
  }

  stop(): void {
    this.generation++;
    this.session = null;
    this.configRequest?.unsubscribe();
    this.sessionProbe?.unsubscribe();
    this.closeSource();
    this.features.set({ notifications: false, postEngagement: false });
    this.configState.set('idle');
    this.lastEventId.set('');
  }

  private closeSource(): void {
    if (this.retryTimer) clearTimeout(this.retryTimer);
    this.retryTimer = undefined;
    this.source?.close();
    this.source = null;
    this.connectionState.set('closed');
  }

  private connect(): void {
    this.closeSource();
    if (!this.session || !isPlatformBrowser(this.platform)) return;
    const channels: string[] = [];
    if (this.features().notifications) channels.push('notifications');
    if (this.communityActive && this.features().postEngagement) channels.push('post-engagement');
    if (!channels.length) return;
    this.connectionState.set('connecting');
    const source = new EventSource('/api/v1/notification/events/stream?channels=' + channels.join(','), { withCredentials: true });
    this.source = source;
    source.addEventListener('connected', () => {
      if (this.source !== source) return;
      this.connectionState.set('open');
      if (this.retryTimer) clearTimeout(this.retryTimer);
      this.retryTimer = undefined;
      this.connected.next();
    });
    for (const type of ['post.reaction.updated', 'post.comment.updated', 'notification.created', 'notification.read', 'notification.unread-count.changed']) {
      source.addEventListener(type, (message: MessageEvent<string>) => {
        if (this.source !== source) return;
        try {
          const event = JSON.parse(message.data) as RealtimeEvent;
          if (event.schemaVersion !== 1 || !event.eventId || !Number.isSafeInteger(event.version) || !event.payload) return;
          this.lastEventId.set(message.lastEventId);
          this.events.next(event);
        } catch { /* An invalid frame is repaired by the next REST synchronization. */ }
      });
    }
    source.onerror = () => {
      if (this.source !== source) return;
      this.connectionState.set('reconnecting');
      // EventSource cannot invoke the HTTP token-refresh interceptor. A REST probe can.
      if (!this.retryTimer) this.retryTimer = setTimeout(() => {
        this.retryTimer = undefined;
        const generation = this.generation;
        this.sessionProbe = this.authApi.me().subscribe({
          next: (response) => {
            if (generation !== this.generation) return;
            if (isBrandAccount(response.result) && !canUseAppFeatures(response.result)) { this.stop(); return; }
            if (generation === this.generation && this.session && this.connectionState() !== 'open') this.connect();
          },
          error: () => {
            if (generation !== this.generation) return;
            if (source.readyState === EventSource.CLOSED) this.connect();
          },
        });
      }, 3000);
    };
  }
}
