import { CommonModule } from '@angular/common';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import {
  Observable,
  catchError,
  finalize,
  forkJoin,
  from,
  map,
  mergeMap,
  of,
  shareReplay,
  switchMap,
  tap,
  toArray,
} from 'rxjs';
import {
  PageView,
  PostApiService,
  PostReportReason,
  PostReportStatus,
  PostReportView,
  PostTopicView,
  PostTypeView,
  PostView,
} from '../../../core/api/post-api.service';
import {
  BrandProfilePublicResponse,
  FarmerProfilePublicResponse,
  ProfileApiService,
} from '../../../core/api/profile-api.service';
import { ApiResponse, apiErrorMessage, unwrapApiResult } from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';
import { UserAvatarComponent } from '../../../shared/user-avatar/user-avatar.component';

interface StatusFilter {
  value: PostReportStatus | 'ALL';
  label: string;
}

interface ReportUser {
  id: string;
  name: string;
  avatarUrl: string | null;
  role: 'FARMER' | 'BRAND' | 'UNKNOWN';
  roleLabel: string;
  details: string[];
}

type CompletionAction = 'RESOLVE' | 'REJECT';
type ModerationAction = 'HIDE' | 'DELETE';

const STATUS_LABELS: Record<PostReportStatus, string> = {
  PENDING: 'Chờ xử lý',
  UNDER_REVIEW: 'Đang xem xét',
  RESOLVED: 'Đã giải quyết',
  REJECTED: 'Đã bác bỏ',
};

const REASON_LABELS: Record<PostReportReason, string> = {
  SPAM: 'Spam hoặc quảng cáo',
  HARASSMENT: 'Quấy rối',
  HATE_SPEECH: 'Ngôn từ thù ghét',
  VIOLENCE: 'Bạo lực',
  SEXUAL_CONTENT: 'Nội dung tình dục',
  MISINFORMATION: 'Thông tin sai lệch',
  COPYRIGHT: 'Vi phạm bản quyền',
  OTHER: 'Lý do khác',
};

@Component({
  selector: 'app-admin-post-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, UserAvatarComponent],
  templateUrl: './post-reports.html',
  styleUrl: './post-reports.scss',
})
export class AdminPostReports {
  private readonly postApi = inject(PostApiService);
  private readonly profileApi = inject(ProfileApiService);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly pageSize = 20;
  private readonly maxConcurrentRequests = 4;
  private readonly userCache = new Map<string, ReportUser>();
  private readonly userRequests = new Map<string, Observable<ReportUser>>();
  private listRequestId = 0;

  readonly statusFilters: StatusFilter[] = [
    { value: 'PENDING', label: 'Chờ xử lý' },
    { value: 'UNDER_REVIEW', label: 'Đang xem xét' },
    { value: 'RESOLVED', label: 'Đã giải quyết' },
    { value: 'REJECTED', label: 'Đã bác bỏ' },
    { value: 'ALL', label: 'Tất cả' },
  ];

  readonly selectedStatus = signal<PostReportStatus | 'ALL'>('PENDING');
  readonly reports = signal<PostReportView[]>([]);
  readonly postsById = signal<Record<string, PostView | null>>({});
  readonly usersById = signal<Record<string, ReportUser>>({});
  readonly postTypes = signal<PostTypeView[]>([]);
  readonly postTopics = signal<PostTopicView[]>([]);
  readonly currentPage = signal(0);
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);
  readonly hasNextPage = signal(false);
  readonly loading = signal(true);
  readonly enrichmentLoading = signal(false);
  readonly loadError = signal('');
  readonly actionReportId = signal<string | null>(null);
  readonly detailReport = signal<PostReportView | null>(null);
  readonly detailLoading = signal(false);
  readonly completionAction = signal<CompletionAction | null>(null);
  readonly completionTarget = signal<PostReportView | null>(null);
  readonly completionSaving = signal(false);
  readonly displayedRange = computed(() => {
    if (!this.totalElements()) return '0 báo cáo';
    const start = this.currentPage() * this.pageSize + 1;
    const end = Math.min(start + this.reports().length - 1, this.totalElements());
    return `${start}–${end} trên ${this.totalElements()} báo cáo`;
  });

  resolutionNote = '';
  moderationAction: ModerationAction = 'HIDE';

  constructor() {
    this.loadCatalogs();
    this.loadReports(0);
  }

  selectStatus(status: PostReportStatus | 'ALL'): void {
    if (status === this.selectedStatus()) return;
    this.selectedStatus.set(status);
    this.loadReports(0);
  }

  loadReports(page = this.currentPage()): void {
    const requestId = ++this.listRequestId;
    this.loading.set(true);
    this.loadError.set('');
    const status = this.selectedStatus();
    this.postApi
      .listPostReports({ status: status === 'ALL' ? null : status, page, size: this.pageSize })
      .pipe(
        finalize(() => {
          if (requestId === this.listRequestId) this.loading.set(false);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (response) => {
          if (requestId !== this.listRequestId) return;
          const result = unwrapApiResult<PageView<PostReportView>>(response);
          if (!result) {
            this.reports.set([]);
            this.loadError.set('Máy chủ không trả về danh sách báo cáo.');
            return;
          }
          const reports = result.items ?? [];
          this.reports.set(reports);
          this.postsById.set({});
          this.usersById.set({});
          this.currentPage.set(result.page);
          this.totalElements.set(result.totalElements);
          this.totalPages.set(result.totalPages);
          this.hasNextPage.set(result.hasNext);
          this.enrichReports(reports, requestId);
        },
        error: (error) => {
          if (requestId !== this.listRequestId) return;
          this.reports.set([]);
          this.loadError.set(apiErrorMessage(error, 'Không thể tải danh sách báo cáo bài viết.'));
        },
      });
  }

  previousPage(): void {
    if (this.currentPage() > 0 && !this.loading()) this.loadReports(this.currentPage() - 1);
  }

  nextPage(): void {
    if (this.hasNextPage() && !this.loading()) this.loadReports(this.currentPage() + 1);
  }

  viewDetail(report: PostReportView): void {
    this.detailReport.set(report);
    this.detailLoading.set(true);
    forkJoin({
      report: this.postApi.getPostReport(report.id).pipe(
        map((response) => unwrapApiResult<PostReportView>(response) ?? null),
        catchError(() => of(null)),
      ),
      post: this.postApi.getAdminPost(report.postId).pipe(
        map((response) => unwrapApiResult<PostView>(response) ?? null),
        catchError(() => of(null)),
      ),
    })
      .pipe(
        switchMap((result) => {
          const latestReport = result.report ?? report;
          const userIds = [latestReport.reporterId, result.post?.authorUserId].filter(
            (id): id is string => !!id,
          );
          return this.resolveUsers(userIds).pipe(map((users) => ({ ...result, latestReport, users })));
        }),
        finalize(() => this.detailLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ report: latest, post, latestReport, users }) => {
          this.detailReport.set(latestReport);
          if (!latest) this.toast.error('Không thể đồng bộ dữ liệu báo cáo mới nhất.');
          this.postsById.update((current) => ({ ...current, [report.postId]: post }));
          this.mergeUsers(users);
          if (!post) this.toast.error('Không thể tải nội dung bài viết được báo cáo.');
        },
        error: (error) =>
          this.toast.error(apiErrorMessage(error, 'Không thể tải chi tiết báo cáo.')),
      });
  }

  closeDetail(): void {
    if (!this.detailLoading() && !this.actionReportId()) this.detailReport.set(null);
  }

  closeDetailFromBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.closeDetail();
  }

  startReview(report: PostReportView): void {
    if (report.status !== 'PENDING' || this.actionReportId()) return;
    this.runReportAction(
      report,
      this.postApi.startPostReportReview(report.id),
      'Đã chuyển báo cáo sang trạng thái đang xem xét.',
    );
  }

  openCompletion(report: PostReportView, action: CompletionAction): void {
    if (!this.isOpen(report) || this.actionReportId()) return;
    this.completionTarget.set(report);
    this.completionAction.set(action);
    this.resolutionNote = report.resolutionNote ?? '';
    this.moderationAction = 'HIDE';
  }

  closeCompletion(): void {
    if (this.completionSaving()) return;
    this.completionTarget.set(null);
    this.completionAction.set(null);
    this.resolutionNote = '';
    this.moderationAction = 'HIDE';
  }

  closeCompletionFromBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.closeCompletion();
  }

  submitCompletion(): void {
    const report = this.completionTarget();
    const action = this.completionAction();
    if (!report || !action || this.completionSaving()) return;
    const payload = {
      resolutionNote: this.resolutionNote.trim() || null,
      ...(action === 'RESOLVE' ? { moderationAction: this.moderationAction } : {}),
    };
    const request =
      action === 'RESOLVE'
        ? this.postApi.resolvePostReport(report.id, payload)
        : this.postApi.rejectPostReport(report.id, payload);

    this.completionSaving.set(true);
    request
      .pipe(
        finalize(() => this.completionSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (response) => {
          const updated = unwrapApiResult<PostReportView>(response);
          if (!updated) {
            this.toast.error('Máy chủ không trả về báo cáo sau khi xử lý.');
            return;
          }
          this.detailReport.set(
            this.detailReport()?.id === updated.id ? updated : this.detailReport(),
          );
          const successMessage = action === 'RESOLVE'
            ? this.moderationAction === 'DELETE'
              ? 'Đã giải quyết báo cáo và xóa bài viết.'
              : 'Đã giải quyết báo cáo và ẩn bài viết.'
            : 'Đã bác bỏ báo cáo.';
          this.closeCompletionAfterSave();
          this.toast.success(successMessage);
          this.reloadCurrentPageAfterMutation();
        },
        error: (error) =>
          this.toast.error(apiErrorMessage(error, 'Không thể hoàn tất xử lý báo cáo.')),
      });
  }

  postFor(report: PostReportView): PostView | null | undefined {
    return this.postsById()[report.postId];
  }

  reporterFor(report: PostReportView): ReportUser {
    return this.usersById()[report.reporterId] ?? this.fallbackUser(report.reporterId);
  }

  authorFor(report: PostReportView): ReportUser {
    const authorId = this.postFor(report)?.authorUserId;
    return authorId
      ? this.usersById()[authorId] ?? this.fallbackUser(authorId)
      : this.fallbackUser('');
  }

  isOpen(report: PostReportView): boolean {
    return report.status === 'PENDING' || report.status === 'UNDER_REVIEW';
  }

  statusLabel(status: PostReportStatus): string {
    return STATUS_LABELS[status];
  }

  reasonLabel(reason: PostReportReason): string {
    return REASON_LABELS[reason];
  }

  postTypeLabel(post: PostView): string {
    return this.postTypes().find((item) => item.id === post.postTypeId)?.name ?? 'Bài viết cộng đồng';
  }

  topicLabel(post: PostView): string | null {
    if (!post.topicId) return null;
    return this.postTopics().find((item) => item.id === post.topicId)?.name ?? null;
  }

  postStatusLabel(post: PostView): string {
    if (post.deletedAt) return 'Đã xóa';
    return { DRAFT: 'Bản nháp', PUBLISHED: 'Đang hiển thị', HIDDEN: 'Đã ẩn' }[post.status];
  }

  visibilityLabel(post: PostView): string {
    return { PUBLIC: 'Công khai', PRIVATE: 'Riêng tư', FOLLOWERS: 'Người theo dõi' }[
      post.visibility
    ];
  }

  formatDate(value: string | null | undefined): string {
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

  completionTitle(): string {
    return this.completionAction() === 'RESOLVE' ? 'Giải quyết báo cáo' : 'Bác bỏ báo cáo';
  }

  completionDescription(): string {
    return this.completionAction() === 'RESOLVE'
      ? 'Chọn cách xử lý bài viết. Hệ thống sẽ thông báo kết quả cho người báo cáo và chủ bài viết.'
      : 'Bài viết được giữ nguyên. Hệ thống sẽ thông báo kết quả cho người báo cáo và chủ bài viết.';
  }

  private loadCatalogs(): void {
    forkJoin({
      types: this.postApi.listPostTypes().pipe(catchError(() => of({ result: [] }))),
      topics: this.postApi.listPostTopics().pipe(catchError(() => of({ result: [] }))),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(({ types, topics }) => {
        this.postTypes.set(unwrapApiResult<PostTypeView[]>(types) ?? []);
        this.postTopics.set(unwrapApiResult<PostTopicView[]>(topics) ?? []);
      });
  }

  private enrichReports(reports: PostReportView[], requestId: number): void {
    if (!reports.length) {
      this.enrichmentLoading.set(false);
      return;
    }
    this.enrichmentLoading.set(true);
    const postIds = [...new Set(reports.map((report) => report.postId))];
    from(postIds)
      .pipe(
        mergeMap(
          (postId) =>
            this.postApi.getAdminPost(postId).pipe(
              map((response) => ({ postId, post: unwrapApiResult<PostView>(response) ?? null })),
              catchError(() => of({ postId, post: null })),
            ),
          this.maxConcurrentRequests,
        ),
        toArray(),
        switchMap((entries) => {
          const userIds = [
            ...reports.map((report) => report.reporterId),
            ...entries.map((entry) => entry.post?.authorUserId).filter((id): id is string => !!id),
          ];
          return this.resolveUsers(userIds).pipe(map((users) => ({ entries, users })));
        }),
        finalize(() => {
          if (requestId === this.listRequestId) this.enrichmentLoading.set(false);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ entries, users }) => {
        if (requestId !== this.listRequestId) return;
        this.postsById.set(
          Object.fromEntries(entries.map((entry) => [entry.postId, entry.post])),
        );
        this.mergeUsers(users);
      });
  }

  private resolveUsers(userIds: string[]): Observable<Map<string, ReportUser>> {
    const uniqueIds = [...new Set(userIds.filter(Boolean))];
    if (!uniqueIds.length) return of(new Map());
    return from(uniqueIds).pipe(
      mergeMap((userId) => this.resolveUser(userId), this.maxConcurrentRequests),
      toArray(),
      map((users) => new Map(users.map((user) => [user.id, user]))),
    );
  }

  private resolveUser(userId: string): Observable<ReportUser> {
    const cached = this.userCache.get(userId);
    if (cached) return of(cached);
    const inFlight = this.userRequests.get(userId);
    if (inFlight) return inFlight;

    const request$ = this.profileApi.getPublicFarmerProfileByUserId(userId).pipe(
      map((response) => {
        if (!response.result) throw new Error('Farmer profile not found');
        return this.farmerUser(userId, response.result);
      }),
      catchError(() =>
        this.profileApi.getPublicBrandProfileByUserId(userId).pipe(
          map((response) => {
            if (!response.result) throw new Error('Brand profile not found');
            return this.brandUser(userId, response.result);
          }),
        ),
      ),
      catchError(() => of(this.fallbackUser(userId))),
      tap((user) => this.userCache.set(userId, user)),
      finalize(() => this.userRequests.delete(userId)),
      shareReplay({ bufferSize: 1, refCount: false }),
    );
    this.userRequests.set(userId, request$);
    return request$;
  }

  private farmerUser(userId: string, profile: FarmerProfilePublicResponse): ReportUser {
    const gender = { MALE: 'Nam', FEMALE: 'Nữ', OTHER: 'Khác' }[profile.gender] ?? profile.gender;
    const area = [profile.provinceId, profile.communeId].filter(Boolean).join(' · ');
    return {
      id: userId,
      name: [profile.firstName, profile.lastName].filter(Boolean).join(' ') || 'Nông dân Nông Thịnh',
      avatarUrl: profile.avatarUrl,
      role: 'FARMER',
      roleLabel: 'Nông dân',
      details: [gender && `Giới tính: ${gender}`, area && `Khu vực: ${area}`].filter(Boolean) as string[],
    };
  }

  private brandUser(userId: string, profile: BrandProfilePublicResponse): ReportUser {
    return {
      id: userId,
      name: profile.brandName || 'Thương hiệu Nông Thịnh',
      avatarUrl: profile.logoUrl,
      role: 'BRAND',
      roleLabel: 'Thương hiệu',
      details: [
        profile.representativeName && `Đại diện: ${profile.representativeName}`,
        profile.websiteUrl && `Website: ${profile.websiteUrl}`,
        profile.description,
      ].filter(Boolean) as string[],
    };
  }

  private fallbackUser(userId: string): ReportUser {
    return {
      id: userId,
      name: userId ? `Người dùng ${this.shortId(userId)}` : 'Chưa xác định',
      avatarUrl: null,
      role: 'UNKNOWN',
      roleLabel: 'Chưa xác định',
      details: [],
    };
  }

  private mergeUsers(users: Map<string, ReportUser>): void {
    this.usersById.update((current) => ({ ...current, ...Object.fromEntries(users) }));
  }

  private runReportAction(
    report: PostReportView,
    request: Observable<ApiResponse<PostReportView>>,
    successMessage: string,
  ): void {
    this.actionReportId.set(report.id);
    request
      .pipe(
        finalize(() => this.actionReportId.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (response) => {
          const updated = unwrapApiResult<PostReportView>(response);
          if (!updated) {
            this.toast.error('Máy chủ không trả về báo cáo sau khi cập nhật.');
            return;
          }
          this.detailReport.set(
            this.detailReport()?.id === updated.id ? updated : this.detailReport(),
          );
          this.toast.success(successMessage);
          this.reloadCurrentPageAfterMutation();
        },
        error: (error) =>
          this.toast.error(apiErrorMessage(error, 'Không thể cập nhật trạng thái báo cáo.')),
      });
  }

  private closeCompletionAfterSave(): void {
    this.completionTarget.set(null);
    this.completionAction.set(null);
    this.resolutionNote = '';
    this.moderationAction = 'HIDE';
  }

  private reloadCurrentPageAfterMutation(): void {
    const targetPage =
      this.reports().length === 1 && this.currentPage() > 0
        ? this.currentPage() - 1
        : this.currentPage();
    this.loadReports(targetPage);
  }
}
