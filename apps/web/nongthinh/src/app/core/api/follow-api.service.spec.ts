import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { AuthService } from '../auth/auth.service';
import { FollowApiService } from './follow-api.service';

describe('FollowApiService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(),
      { provide: AuthService, useValue: { currentUser: signal({ userId: 'viewer' }) } },
    ] });
  });
  afterEach(() => TestBed.inject(HttpTestingController).verify());
  it('updates the shared badge only after successful follow and preserves it on failed unfollow', () => {
    const api = TestBed.inject(FollowApiService);
    const http = TestBed.inject(HttpTestingController);
    TestBed.tick();
    api.setFollowing('author', true).subscribe();
    expect(api.pending()['author']).toBe(true);
    expect(api.following()['author']).toBeUndefined();
    http.expectOne('/api/v1/profile/users/author/follow').flush({ result: {} });
    expect(api.following()['author']).toBe(true);
    expect(api.pending()['author']).toBe(false);
    api.setFollowing('author', false).subscribe({ error: () => {} });
    const request = http.expectOne('/api/v1/profile/users/author/follow');
    expect(request.request.method).toBe('DELETE');
    request.flush({}, { status: 503, statusText: 'Unavailable' });
    expect(api.following()['author']).toBe(true);
    expect(api.pending()['author']).toBe(false);
  });
  it('loads relationship status in a batch', () => {
    const api = TestBed.inject(FollowApiService);
    TestBed.tick();
    api.statuses(['a', 'b']).subscribe();
    TestBed.inject(HttpTestingController).expectOne(request =>
      request.url.endsWith('/following-status') && request.params.get('userIds') === 'a,b'
    ).flush({ result: ['b'] });
    expect(api.following()).toEqual({ a: false, b: true });
  });
});
