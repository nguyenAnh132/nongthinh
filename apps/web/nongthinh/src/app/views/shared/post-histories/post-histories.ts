import { CommonModule } from '@angular/common';
import { Component, DestroyRef, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import {
  POST_HISTORY_ACTIONS,
  PageView,
  PostApiService,
  PostHistoryAction,
  PostHistoryActorType,
  PostHistoryView,
  PostStatus,
  PostVisibility,
} from '../../../core/api/post-api.service';
import { apiErrorMessage, unwrapApiResult } from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';

interface ActionFilter {
  value: PostHistoryAction | 'ALL';
  label: string;
}

const ACTION_LABELS: Record<PostHistoryAction, string> = {
  CREATED: 'Đã tạo',
  UPDATED: 'Đã cập nhật',
  PUBLISHED: 'Đã xuất bản',
  VISIBILITY_CHANGED: 'Đổi quyền xem',
  HIDDEN: 'Đã ẩn',
  RESTORED: 'Đã khôi phục',
  DELETED: 'Đã xóa',
};

const STATUS_LABELS: Record<PostStatus, string> = {
  DRAFT: 'Bản nháp',
  PUBLISHED: 'Đã xuất bản',
  HIDDEN: 'Đã ẩn',
};

const VISIBILITY_LABELS: Record<PostVisibility, string> = {
  PUBLIC: 'Công khai',
  PRIVATE: 'Riêng tư',
  FOLLOWERS: 'Người theo dõi',
};

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

@Component({
  selector: 'app-post-histories',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './post-histories.html',
  styleUrl: './post-histories.scss',
})
export class PostHistories {
  private readonly postApi = inject(PostApiService);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly pageSize = 20;

  readonly embedded = input(false);
  readonly userEmails = input<Record<string, string>>({});

  readonly actionFilters: ActionFilter[] = [
    { value: 'ALL', label: 'Tất cả' },
    ...POST_HISTORY_ACTIONS.map((value) => ({ value, label: ACTION_LABELS[value] })),
  ];
  readonly actorTypes: Array<{ value: PostHistoryActorType | ''; label: string }> = [
    { value: '', label: 'Tất cả tác nhân' },
    { value: 'USER', label: 'Người dùng' },
    { value: 'ADMIN', label: 'Quản trị viên' },
    { value: 'SYSTEM', label: 'Hệ thống' },
  ];

  readonly selectedAction = signal<PostHistoryAction | 'ALL'>('ALL');
  readonly histories = signal<PostHistoryView[]>([]);
  readonly currentPage = signal(0);
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);
  readonly hasNextPage = signal(false);
  readonly loading = signal(true);
  readonly loadError = signal('');
  readonly detail = signal<PostHistoryView | null>(null);
  readonly detailLoading = signal(false);
  readonly displayedRange = computed(() => {
    if (!this.totalElements()) return '0 bản ghi';
    const start = this.currentPage() * this.pageSize + 1;
    const end = Math.min(start + this.histories().length - 1, this.totalElements());
    return `${start}–${end} trên ${this.totalElements()} bản ghi`;
  });

  postIdFilter = '';
  postAuthorUserIdFilter = '';
  actorUserIdFilter = '';
  actorTypeFilter: PostHistoryActorType | '' = '';

  constructor() {
    this.loadHistories(0);
  }

  selectAction(action: PostHistoryAction | 'ALL'): void {
    if (action === this.selectedAction()) return;
    this.selectedAction.set(action);
    this.loadHistories(0);
  }

  applyFilters(): void {
    this.loadHistories(0);
  }

  clearFilters(): void {
    this.postIdFilter = '';
    this.postAuthorUserIdFilter = '';
    this.actorUserIdFilter = '';
    this.actorTypeFilter = '';
    this.selectedAction.set('ALL');
    this.loadHistories(0);
  }

  loadHistories(page = this.currentPage()): void {
    const validationError = this.filterValidationError();
    if (validationError) {
      this.loading.set(false);
      this.loadError.set(validationError);
      return;
    }
    this.loading.set(true);
    this.loadError.set('');
    const selectedAction = this.selectedAction();
    const action: PostHistoryAction | null = selectedAction === 'ALL' ? null : selectedAction;
    const request = this.postApi.listAdminPostHistories({
      postId: this.normalized(this.postIdFilter),
      postAuthorUserId: this.normalized(this.postAuthorUserIdFilter),
      actorUserId: this.normalized(this.actorUserIdFilter),
      actorType: this.actorTypeFilter || null,
      action,
      page,
      size: this.pageSize,
    });

    request
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (response) => {
          const result = unwrapApiResult<PageView<PostHistoryView>>(response);
          if (!result) {
            this.resetPage();
            this.loadError.set('Máy chủ không trả về danh sách lịch sử bài viết.');
            return;
          }
          this.histories.set(result.items ?? []);
          this.currentPage.set(result.page);
          this.totalElements.set(result.totalElements);
          this.totalPages.set(result.totalPages);
          this.hasNextPage.set(result.hasNext);
        },
        error: (error) => {
          this.resetPage();
          this.loadError.set(apiErrorMessage(error, 'Không thể tải danh sách lịch sử bài viết.'));
        },
      });
  }

  previousPage(): void {
    if (this.currentPage() > 0 && !this.loading()) this.loadHistories(this.currentPage() - 1);
  }

  nextPage(): void {
    if (this.hasNextPage() && !this.loading()) this.loadHistories(this.currentPage() + 1);
  }

  viewDetail(history: PostHistoryView): void {
    this.detail.set(history);
    this.detailLoading.set(true);
    this.postApi
      .getAdminPostHistory(history.id)
      .pipe(
        finalize(() => this.detailLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (response) => {
          const value = unwrapApiResult<PostHistoryView>(response);
          if (value) this.detail.set(value);
          else this.toast.error('Máy chủ không trả về chi tiết lịch sử bài viết.');
        },
        error: (error) =>
          this.toast.error(apiErrorMessage(error, 'Không thể tải chi tiết lịch sử bài viết.')),
      });
  }

  closeDetail(): void {
    if (!this.detailLoading()) this.detail.set(null);
  }

  closeDetailFromBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.closeDetail();
  }

  actionLabel(action: PostHistoryAction): string {
    return ACTION_LABELS[action];
  }

  actorLabel(actorType: PostHistoryActorType): string {
    if (actorType === 'ADMIN') return 'Quản trị viên';
    if (actorType === 'SYSTEM') return 'Hệ thống';
    return 'Người dùng';
  }

  statusLabel(status: PostStatus | null): string {
    return status ? STATUS_LABELS[status] : '—';
  }

  visibilityLabel(visibility: PostVisibility | null): string {
    return visibility ? VISIBILITY_LABELS[visibility] : '—';
  }

  stateTransition(history: PostHistoryView): string {
    if (!history.previousStatus) return this.statusLabel(history.newStatus);
    if (history.previousStatus === history.newStatus) return this.statusLabel(history.newStatus);
    return `${this.statusLabel(history.previousStatus)} → ${this.statusLabel(history.newStatus)}`;
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

  shortId(value: string | null): string {
    if (!value) return '—';
    return value.length > 12 ? `${value.slice(0, 8)}…${value.slice(-4)}` : value;
  }

  userLabel(userId: string | null): string {
    if (!userId) return '—';
    return this.userEmails()[userId] ?? this.shortId(userId);
  }

  private normalized(value: string): string | null {
    return value.trim() || null;
  }

  private filterValidationError(): string {
    const invalidField = [
      ['Mã bài viết', this.postIdFilter],
      ['Mã tác giả', this.postAuthorUserIdFilter],
      ['Mã tác nhân', this.actorUserIdFilter],
    ].find(([, value]) => value.trim() && !UUID_PATTERN.test(value.trim()));
    return invalidField ? `${invalidField[0]} phải là UUID hợp lệ.` : '';
  }

  private resetPage(): void {
    this.histories.set([]);
    this.currentPage.set(0);
    this.totalElements.set(0);
    this.totalPages.set(0);
    this.hasNextPage.set(false);
  }
}
