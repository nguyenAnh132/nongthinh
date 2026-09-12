import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { NotificationStore } from '../../core/realtime/notification.store';
import { NotificationPopover } from './notification-popover';

describe('NotificationPopover availability', () => {
  function create(configState: 'loading' | 'error' | 'ready', enabled = false, unread = 0) {
    const store = {
      enabled: signal(enabled), configState: signal(configState), unreadCount: signal(unread),
      badge: signal(unread > 99 ? '99+' : String(unread)), connectionState: signal('closed'),
      items: signal([]), error: signal(''), loading: signal(false), hasMore: signal(false),
      refresh: vi.fn(), retryConnection: vi.fn(), markAllRead: vi.fn(),
    };
    TestBed.configureTestingModule({ imports: [NotificationPopover], providers: [
      provideRouter([]), { provide: NotificationStore, useValue: store },
    ] });
    const fixture = TestBed.createComponent(NotificationPopover);
    fixture.detectChanges();
    const bell = fixture.nativeElement.querySelector('.notification-button') as HTMLButtonElement;
    expect(bell).not.toBeNull();
    bell.click();
    fixture.detectChanges();
    return { fixture, store, bell };
  }

  it('keeps the bell visible while configuration is loading', () => {
    const { fixture } = create('loading');
    expect(fixture.nativeElement.textContent).toContain('Đang kết nối dịch vụ thông báo');
    expect(fixture.nativeElement.textContent).not.toContain('Bạn chưa có thông báo');
  });

  it('keeps the bell visible and exposes retry when configuration fails', () => {
    const { fixture, store } = create('error');
    expect(fixture.nativeElement.textContent).toContain('Không thể kết nối dịch vụ thông báo');
    fixture.nativeElement.querySelector('[role="alert"] button').click();
    expect(store.retryConnection).toHaveBeenCalledOnce();
  });

  it('explains a disabled feature and disables read-all without showing a stale badge', () => {
    const { fixture } = create('ready', false, 10);
    expect(fixture.nativeElement.textContent).toContain('Thông báo chưa được bật');
    expect(fixture.nativeElement.querySelector('header button').disabled).toBe(true);
    expect(fixture.nativeElement.querySelector('.notification-button i')).toBeNull();
  });

  it('shows the enabled empty state without an unread badge and closes with Escape', () => {
    const { fixture, store, bell } = create('ready', true);
    expect(store.refresh).toHaveBeenCalledOnce();
    expect(fixture.nativeElement.textContent).toContain('Bạn chưa có thông báo nào');
    expect(fixture.nativeElement.querySelector('.notification-button i')).toBeNull();
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.notification-panel')).toBeNull();
    expect(bell.getAttribute('aria-expanded')).toBe('false');
  });

  it('renders the 99+ unread badge when notifications are available', () => {
    const { fixture } = create('ready', true, 105);
    expect(fixture.nativeElement.querySelector('.notification-button i').textContent).toBe('99+');
  });
});
