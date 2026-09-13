import { HttpClient } from '@angular/common/http';
import { Injectable, computed, effect, inject, signal } from '@angular/core';
import { finalize, tap } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { ApiResponse } from '../models/api-response';

export interface FollowProfile {
  userId: string;
  displayName: string;
  avatarUrl: string | null;
  role: string;
  followerCount: number;
  followingCount: number;
  following: boolean;
}
export interface FollowPage {
  items: FollowProfile[];
  page: number;
  size: number;
  hasNext: boolean;
}
@Injectable({ providedIn: 'root' })
export class FollowApiService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly viewer = computed(() => this.auth.currentUser()?.userId);
  private readonly base = '/api/v1/profile/users';
  readonly following = signal<Record<string, boolean>>({});
  readonly pending = signal<Record<string, boolean>>({});
  readonly revision = signal(0);

  constructor() {
    effect(() => {
      this.viewer();
      this.following.set({});
      this.pending.set({});
    });
  }
  profile(userId: string) {
    return this.http.get<ApiResponse<FollowProfile>>(`${this.base}/${userId}/follow-profile`).pipe(
      tap(response => {
        if (response.result) this.remember([response.result]);
      }),
    );
  }
  list(userId: string, kind: 'followers' | 'following', page: number) {
    return this.http.get<ApiResponse<FollowPage>>(`${this.base}/${userId}/${kind}`, {
      params: { page, size: 20 },
    }).pipe(tap(response => this.remember(response.result?.items ?? [])));
  }
  statuses(userIds: string[]) {
    const viewer = this.auth.currentUser()?.userId;
    return this.http.get<ApiResponse<string[]>>(`${this.base}/following-status`, {
      params: { userIds: userIds.join(',') },
    }).pipe(tap(response => {
      if (viewer !== this.auth.currentUser()?.userId) return;
      const followed = new Set(response.result ?? []);
      this.following.update(current => ({
        ...current, ...Object.fromEntries(userIds.map(id => [id, followed.has(id)])),
      }));
    }));
  }
  setFollowing(userId: string, following: boolean) {
    const viewer = this.auth.currentUser()?.userId;
    this.pending.update(current => ({ ...current, [userId]: true }));
    const request = following
      ? this.http.put<ApiResponse<FollowProfile | null>>(`${this.base}/${userId}/follow`, {})
      : this.http.delete<ApiResponse<FollowProfile | null>>(`${this.base}/${userId}/follow`);
    return request.pipe(
      tap(() => {
        if (viewer !== this.auth.currentUser()?.userId) return;
        this.following.update(current => ({ ...current, [userId]: following }));
        this.revision.update(value => value + 1);
      }),
      finalize(() => this.pending.update(current => ({ ...current, [userId]: false }))),
    );
  }
  private remember(items: FollowProfile[]) {
    this.following.update(current => ({
      ...current, ...Object.fromEntries(items.map(item => [item.userId, item.following])),
    }));
  }
}
