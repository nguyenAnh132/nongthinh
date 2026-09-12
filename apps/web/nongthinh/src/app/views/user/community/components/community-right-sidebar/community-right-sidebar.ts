import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { PostApiService, TrendingPostTopicView } from '../../../../../core/api/post-api.service';

@Component({
  selector: 'app-community-right-sidebar',
  standalone: true,
  templateUrl: './community-right-sidebar.html',
  styleUrl: './community-right-sidebar.scss',
})
export class CommunityRightSidebar {
  private readonly postApi = inject(PostApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly topics = signal<TrendingPostTopicView[]>([]);
  readonly loading = signal(true);
  readonly loadFailed = signal(false);

  constructor() {
    this.postApi
      .listTrendingPostTopics(3)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.topics.set(response.result ?? []);
          this.loading.set(false);
        },
        error: () => {
          this.loadFailed.set(true);
          this.loading.set(false);
        },
      });
  }

  formatRank(rank: number): string {
    return String(rank).padStart(2, '0');
  }
}
