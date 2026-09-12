import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { RealtimeService } from './realtime.service';

class FakeEventSource {
  static instances: FakeEventSource[] = [];
  static CLOSED = 2;
  readonly listeners = new Map<string, (event: MessageEvent) => void>();
  readyState = 1;
  onerror?: () => void;
  close = vi.fn(() => { this.readyState = 2; });
  constructor(readonly url: string, readonly options: EventSourceInit) { FakeEventSource.instances.push(this); }
  addEventListener(name: string, callback: (event: MessageEvent) => void): void { this.listeners.set(name, callback); }
  emit(name: string, data: object = {}): void {
    this.listeners.get(name)?.({ data: JSON.stringify(data), lastEventId: 'event-1' } as MessageEvent);
  }
}

describe('RealtimeService', () => {
  beforeEach(() => {
    FakeEventSource.instances = [];
    vi.stubGlobal('EventSource', FakeEventSource);
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
  });
  afterEach(() => {
    TestBed.inject(RealtimeService).stop();
    TestBed.inject(HttpTestingController).verify();
    vi.unstubAllGlobals();
    vi.useRealTimers();
  });

  it('retries failed config after entering Community and then subscribes to both channels', () => {
    vi.useFakeTimers();
    const service = TestBed.inject(RealtimeService);
    const http = TestBed.inject(HttpTestingController);
    service.start('farmer-1');
    expect(service.configState()).toBe('loading');
    http.expectOne('/api/v1/notification/events/config').flush({}, { status: 503, statusText: 'Unavailable' });
    expect(service.configState()).toBe('error');
    service.setCommunityActive(true);
    vi.advanceTimersByTime(3000);
    http.expectOne('/api/v1/notification/events/config')
      .flush({ result: { notifications: true, postEngagement: true } });
    expect(service.configState()).toBe('ready');
    expect(FakeEventSource.instances).toHaveLength(1);
    expect(FakeEventSource.instances[0].url).toContain('channels=notifications,post-engagement');
  });

  it('allows immediate configuration retry and cancels the scheduled retry', () => {
    vi.useFakeTimers();
    const service = TestBed.inject(RealtimeService);
    const http = TestBed.inject(HttpTestingController);
    service.start('farmer-1');
    http.expectOne('/api/v1/notification/events/config').flush({}, { status: 503, statusText: 'Unavailable' });
    service.retryConfiguration();
    http.expectOne('/api/v1/notification/events/config')
      .flush({ result: { notifications: false, postEngagement: false } });
    vi.advanceTimersByTime(3000);
    http.expectNone('/api/v1/notification/events/config');
    expect(service.configState()).toBe('ready');
    expect(FakeEventSource.instances).toHaveLength(0);
    service.stop();
    expect(service.configState()).toBe('idle');
  });

  it('uses one credentialed stream and reconnects when Community channels change', () => {
    const service = TestBed.inject(RealtimeService);
    service.start('user-1');
    TestBed.inject(HttpTestingController).expectOne('/api/v1/notification/events/config')
      .flush({ result: { notifications: true, postEngagement: true } });
    const first = FakeEventSource.instances[0];
    expect(first.url).toContain('channels=notifications');
    expect(first.options.withCredentials).toBe(true);
    service.start('user-1');
    expect(FakeEventSource.instances).toHaveLength(1);
    service.setCommunityActive(true);
    expect(first.close).toHaveBeenCalledOnce();
    expect(FakeEventSource.instances[1].url).toContain('channels=notifications,post-engagement');
    service.setCommunityActive(false);
    expect(FakeEventSource.instances[1].close).toHaveBeenCalledOnce();
    expect(FakeEventSource.instances[2].url).toContain('channels=notifications');
  });

  it('resynchronizes on reconnect, rejects malformed frames and closes on logout', () => {
    const service = TestBed.inject(RealtimeService);
    const connected = vi.fn();
    const event = vi.fn();
    service.connected.subscribe(connected);
    service.events.subscribe(event);
    service.start('user-1');
    TestBed.inject(HttpTestingController).expectOne('/api/v1/notification/events/config')
      .flush({ result: { notifications: true, postEngagement: false } });
    const source = FakeEventSource.instances[0];
    source.emit('connected');
    source.emit('connected');
    expect(connected).toHaveBeenCalledTimes(2);
    source.emit('notification.created', { schemaVersion: 99 });
    expect(event).not.toHaveBeenCalled();
    source.emit('notification.created', { eventId: 'event-1', schemaVersion: 1, version: 1, payload: {} });
    expect(service.lastEventId()).toBe('event-1');
    service.stop();
    expect(source.close).toHaveBeenCalledOnce();
    expect(service.lastEventId()).toBe('');
    expect(service.connectionState()).toBe('closed');
    source.emit('notification.created', { eventId: 'event-2', schemaVersion: 1, version: 2, payload: {} });
    expect(event).toHaveBeenCalledOnce();
  });
});
