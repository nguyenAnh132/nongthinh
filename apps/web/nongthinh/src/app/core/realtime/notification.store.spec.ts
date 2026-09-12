import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { NotificationStore } from './notification.store';
import { NotificationApiService } from '../api/notification-api.service';
import { RealtimeService } from './realtime.service';
import { RealtimeEvent } from './realtime.models';

describe('NotificationStore', () => {
  const events = new Subject<RealtimeEvent>();
  let api: { listNotifications: ReturnType<typeof vi.fn>; getUnreadNotificationCount: ReturnType<typeof vi.fn>;
    markNotificationRead: ReturnType<typeof vi.fn>; markAllNotificationsRead: ReturnType<typeof vi.fn> };
  beforeEach(() => {
    vi.useFakeTimers();
    api = {
      listNotifications: vi.fn(() => of({ result: { items: [], page: 0, size: 20, hasNext: false } })),
      getUnreadNotificationCount: vi.fn(() => of({ result: { version: 1, unreadCount: 105 } })),
      markNotificationRead: vi.fn(() => of({ result: { version: 2, unreadCount: 104 } })),
      markAllNotificationsRead: vi.fn(() => of({ result: { version: 3, unreadCount: 0 } })),
    };
    TestBed.configureTestingModule({ providers: [
      { provide: NotificationApiService, useValue: api },
      { provide: RealtimeService, useValue: { features: signal({ notifications: true, postEngagement: true }),
        events, connected: new Subject<void>(), connectionState: signal('open'), lastEventId: signal('') } },
    ] });
  });
  afterEach(() => vi.useRealTimers());

  it('formats 99+ and prevents old responses from restoring unread after read-all', () => {
    const store = TestBed.inject(NotificationStore);
    store.setSession('user-1');
    store.refresh();
    vi.advanceTimersByTime(110);
    expect(store.badge()).toBe('99+');
    store.markRead('notification-1');
    expect(store.unreadCount()).toBe(104);
    store.markAllRead();
    expect(store.unreadCount()).toBe(0);
    vi.advanceTimersByTime(110);
    expect(store.unreadCount()).toBe(0);
  });

  it('clears notifications on logout and ignores stale in-flight responses', () => {
    const pending = new Subject<unknown>();
    api.getUnreadNotificationCount.mockReturnValue(pending);
    const store = TestBed.inject(NotificationStore);
    store.setSession('user-1');
    store.refresh();
    vi.advanceTimersByTime(110);
    store.setSession(null);
    pending.next({ result: { version: 5, unreadCount: 20 } });
    pending.complete();
    expect(store.items()).toEqual([]);
    expect(store.unreadCount()).toBe(0);
    expect(store.loading()).toBe(false);
  });
});
