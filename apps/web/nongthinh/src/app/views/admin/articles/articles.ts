import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import {
  PageView,
  PostApiService,
  PostStatisticsBucket,
  PostStatisticsView,
  PostStatus,
  PostView,
} from '../../../core/api/post-api.service';
import { apiErrorMessage, unwrapApiResult } from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';
import { PostHistories } from '../../shared/post-histories/post-histories';

type ManagementView = 'POSTS' | 'HISTORY';
type StatisticsPeriod = 'TODAY' | 'WEEK' | 'CUSTOM';

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const STATUS_LABELS: Record<PostStatus, string> = {
  DRAFT: 'Bản nháp',
  PUBLISHED: 'Đã xuất bản',
  HIDDEN: 'Đã ẩn',
};

@Component({
  selector: 'app-articles',
  standalone: true,
  imports: [CommonModule, FormsModule, PostHistories],
  templateUrl: './articles.html',
  styleUrl: './articles.scss',
})
export class Articles implements OnInit {
  private readonly postApi = inject(PostApiService);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly pageSize = 12;

  readonly activeView = signal<ManagementView>('POSTS');
  readonly period = signal<StatisticsPeriod>('WEEK');
  readonly posts = signal<PostView[]>([]);
  readonly currentPage = signal(0);
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);
  readonly hasNextPage = signal(false);
  readonly loading = signal(true);
  readonly loadError = signal('');
  readonly statistics = signal<PostStatisticsView | null>(null);
  readonly statisticsLoading = signal(true);
  readonly detail = signal<PostView | null>(null);
  readonly detailLoading = signal(false);
  readonly userEmails = signal<Record<string, string>>({});
  readonly chartMaximum = computed(() =>
    Math.max(1, ...(this.statistics()?.timeline ?? []).map((point) => point.count)),
  );
  readonly displayedRange = computed(() => {
    if (!this.totalElements()) return '0 bài viết';
    const start = this.currentPage() * this.pageSize + 1;
    const end = Math.min(start + this.posts().length - 1, this.totalElements());
    return `${start}–${end} trên ${this.totalElements()} bài viết`;
  });

  searchUserQuery = '';
  contentKeyword = '';
  statusFilter: PostStatus | '' = '';
  filterError = '';
  customFrom = this.inputDate(this.startOfLocalDay(new Date()));
  customTo = this.customFrom;

  ngOnInit(): void {
    this.loadPosts(0);
    this.loadStatistics();
  }

  selectView(view: ManagementView): void {
    this.activeView.set(view);
  }

  selectPeriod(period: StatisticsPeriod): void {
    if (this.period() === period && period !== 'CUSTOM') return;
    this.period.set(period);
    if (period !== 'CUSTOM') this.loadStatistics();
  }

  applyCustomRange(): void {
    this.period.set('CUSTOM');
    this.loadStatistics();
  }

  applyFilters(): void {
    this.loadPosts(0);
  }

  clearFilters(): void {
    this.searchUserQuery = '';
    this.contentKeyword = '';
    this.statusFilter = '';
    this.filterError = '';
    this.loadPosts(0);
  }

  loadPosts(page = this.currentPage()): void {
    const authorUserId = this.resolveAuthorId();
    if (this.filterError) return;

    this.loading.set(true);
    this.loadError.set('');
    this.postApi
      .listAdminPosts({
        authorUserId,
        status: this.statusFilter || null,
        keyword: this.contentKeyword.trim() || null,
        page,
        size: this.pageSize,
      })
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (response) => {
          const result = unwrapApiResult<PageView<PostView>>(response);
          if (!result) {
            this.resetPage();
            this.loadError.set('Máy chủ không trả về danh sách bài viết.');
            return;
          }
          this.posts.set(result.items ?? []);
          this.currentPage.set(result.page);
          this.totalElements.set(result.totalElements);
          this.totalPages.set(result.totalPages);
          this.hasNextPage.set(result.hasNext);
        },
        error: (error) => {
          this.resetPage();
          this.loadError.set(apiErrorMessage(error, 'Không thể tải danh sách bài viết.'));
        },
      });
  }

  loadStatistics(): void {
    const range = this.statisticsRange();
    if (!range) return;
    this.statisticsLoading.set(true);
    this.postApi
      .getPostStatistics({
        from: range.from.toISOString(),
        to: range.to.toISOString(),
        bucket: range.bucket,
        timeZone: 'Asia/Ho_Chi_Minh',
      })
      .pipe(
        finalize(() => this.statisticsLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (response) => {
          const value = unwrapApiResult<PostStatisticsView>(response);
          if (value) this.statistics.set(value);
          else this.toast.error('Máy chủ không trả về thống kê bài viết.');
        },
        error: (error) =>
          this.toast.error(apiErrorMessage(error, 'Không thể tải thống kê bài viết.')),
      });
  }

  previousPage(): void {
    if (this.currentPage() > 0 && !this.loading()) this.loadPosts(this.currentPage() - 1);
  }

  nextPage(): void {
    if (this.hasNextPage() && !this.loading()) this.loadPosts(this.currentPage() + 1);
  }

  viewDetail(post: PostView): void {
    this.detail.set(post);
    this.detailLoading.set(true);
    this.postApi
      .getAdminPost(post.id)
      .pipe(
        finalize(() => this.detailLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (response) => {
          const value = unwrapApiResult<PostView>(response);
          if (value) this.detail.set(value);
        },
        error: (error) =>
          this.toast.error(apiErrorMessage(error, 'Không thể tải chi tiết bài viết.')),
      });
  }

  closeDetail(): void {
    if (!this.detailLoading()) this.detail.set(null);
  }

  closeDetailFromBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.closeDetail();
  }

  authorLabel(authorUserId: string): string {
    return this.userEmails()[authorUserId] ?? this.shortId(authorUserId);
  }

  statusLabel(status: PostStatus): string {
    return STATUS_LABELS[status];
  }

  statusCount(status: PostStatus): number {
    return this.statistics()?.statusCounts?.[status] ?? 0;
  }

  chartBarHeight(count: number): number {
    return count === 0 ? 3 : Math.max(8, Math.round((count / this.chartMaximum()) * 100));
  }

  chartLabel(bucketStart: string): string {
    const date = new Date(bucketStart);
    if (this.statistics()?.bucket === 'HOUR') {
      return new Intl.DateTimeFormat('vi-VN', { hour: '2-digit' }).format(date);
    }
    return new Intl.DateTimeFormat('vi-VN', { weekday: 'short', day: '2-digit' }).format(date);
  }

  formatDate(value: string | null): string {
    if (!value) return '—';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return new Intl.DateTimeFormat('vi-VN', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(date);
  }

  shortId(value: string): string {
    return value.length > 12 ? `${value.slice(0, 8)}…${value.slice(-4)}` : value;
  }

  private resolveAuthorId(): string | null {
    this.filterError = '';
    const query = this.searchUserQuery.trim();
    if (!query) return null;
    if (UUID_PATTERN.test(query)) return query;
    this.filterError = 'UUID người đăng không hợp lệ.';
    return null;
  }

  private statisticsRange(): {
    from: Date;
    to: Date;
    bucket: PostStatisticsBucket;
  } | null {
    const today = this.startOfLocalDay(new Date());
    if (this.period() === 'TODAY') {
      return { from: today, to: this.addDays(today, 1), bucket: 'HOUR' };
    }
    if (this.period() === 'WEEK') {
      return { from: this.addDays(today, -6), to: this.addDays(today, 1), bucket: 'DAY' };
    }
    const from = this.parseInputDate(this.customFrom);
    const end = this.parseInputDate(this.customTo);
    if (!from || !end || from > end) {
      this.toast.error('Khoảng thời gian thống kê không hợp lệ.');
      return null;
    }
    const to = this.addDays(end, 1);
    const days = Math.round((to.getTime() - from.getTime()) / 86_400_000);
    return { from, to, bucket: days <= 1 ? 'HOUR' : 'DAY' };
  }

  private startOfLocalDay(value: Date): Date {
    return new Date(value.getFullYear(), value.getMonth(), value.getDate());
  }

  private addDays(value: Date, days: number): Date {
    return new Date(value.getFullYear(), value.getMonth(), value.getDate() + days);
  }

  private inputDate(value: Date): string {
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private parseInputDate(value: string): Date | null {
    const parts = value.split('-').map(Number);
    if (parts.length !== 3 || parts.some((part) => !Number.isInteger(part))) return null;
    const date = new Date(parts[0], parts[1] - 1, parts[2]);
    return Number.isNaN(date.getTime()) ? null : date;
  }

  private resetPage(): void {
    this.posts.set([]);
    this.currentPage.set(0);
    this.totalElements.set(0);
    this.totalPages.set(0);
    this.hasNextPage.set(false);
  }
}
