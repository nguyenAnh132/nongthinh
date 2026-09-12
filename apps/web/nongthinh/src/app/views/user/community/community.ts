import { Component, DestroyRef, HostListener, Injector, afterNextRender, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  Observable,
  catchError,
  combineLatest,
  concatMap,
  finalize,
  forkJoin,
  from,
  map,
  mergeMap,
  of,
  shareReplay,
  switchMap,
  tap,
  timer,
  toArray,
} from 'rxjs';
import { AgriCatalogApiService, CropTypeView } from '../../../core/api/agri-catalog-api.service';
import { FileApiService, FileView } from '../../../core/api/file-api.service';
import {
  POST_REACTION_TYPES,
  PageView,
  PostApiService,
  PostBookmarkStatusView,
  PostCommentThreadView,
  PostCommentView,
  PostReactionSummaryView,
  PostReactionType,
  PostReactionView,
  PostShareSummaryView,
  PostTopicView,
  PostTypeView,
  PostView,
} from '../../../core/api/post-api.service';
import {
  BrandProfilePublicResponse,
  FarmerProfilePublicResponse,
  ProfileApiService,
} from '../../../core/api/profile-api.service';
import { ApiResponse, apiErrorMessage } from '../../../core/models/api-response';
import { UserAvatarComponent } from '../../../shared/user-avatar/user-avatar.component';
import { AuthService } from '../../../core/auth/auth.service';
import { RealtimeService } from '../../../core/realtime/realtime.service';
import { CommentMetrics, ReactionMetrics, RealtimeEvent } from '../../../core/realtime/realtime.models';
import { CommunityRightSidebar } from './components/community-right-sidebar/community-right-sidebar';
import { CommunitySidebar } from './components/community-sidebar/community-sidebar';
import { CreatePostCard } from './components/create-post-card/create-post-card';
import { CreatePostModal } from './components/create-post-modal/create-post-modal';
import {
  CommunityCommentCreateEvent,
  CommunityCommentDeleteEvent,
  CommunityCommentsRequestEvent,
  CommunityCommentUpdateEvent,
  CommunityReactionEvent,
  CommunityPostReportEvent,
  PostFeed,
} from './components/post-feed/post-feed';
import {
  CommunityAuthor,
  CommunityComment,
  CommunityFilter,
  CommunityPost,
  NewCommunityPost,
  PostCategory,
} from './models/community.models';

interface MobileNavItem {
  id: CommunityFilter;
  iconUrl: string;
  label: string;
}

interface ReactionEnrichment {
  summary: PostReactionSummaryView;
  error: string;
}

interface BookmarkEnrichment {
  status: PostBookmarkStatusView;
  error: string;
}

interface ShareEnrichment {
  summary: PostShareSummaryView;
  error: string;
}

@Component({
  selector: 'app-community',
  standalone: true,
  imports: [CommunitySidebar, CommunityRightSidebar, CreatePostCard, CreatePostModal, PostFeed, RouterLink, UserAvatarComponent],
  templateUrl: './community.html',
  styleUrl: './community.scss',
})
export class Community {
  private readonly realtime = inject(RealtimeService);
  private readonly reactionBuffer = new Map<string, ReactionMetrics>();
  private readonly commentBuffer = new Map<string, CommentMetrics>();
  private focusedPostId: string | null = null;
  private readonly authService = inject(AuthService);
  private readonly postApi = inject(PostApiService);
  private readonly agriCatalogApi = inject(AgriCatalogApiService);
  private readonly fileApi = inject(FileApiService);
  private readonly profileApi = inject(ProfileApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly injector = inject(Injector);
  private readonly destroyRef = inject(DestroyRef);

  private readonly pageSize = 20;
  private readonly commentPageSize = 10;
  private readonly maxConcurrentEnrichmentRequests = 4;
  private readonly authorCache = new Map<string, CommunityAuthor>();
  private readonly authorRequests = new Map<string, Observable<CommunityAuthor>>();
  private readonly commentRequestIds = new Map<string, number>();
  private catalogReady = false;
  private feedRequestId = 0;
  private reactionMutationSequence = 0;
  private bookmarkMutationSequence = 0;
  private shareMutationSequence = 0;
  private reactionUsersRequestId = 0;

  readonly detailMode = signal(false);
  readonly reactionDialogOpen = signal(false);
  readonly activeReactionFilter = signal<PostReactionType | null>(null);
  readonly reactionUsers = signal<Array<PostReactionView & { author: CommunityAuthor }>>([]);
  readonly reactionUsersPage = signal(0);
  readonly reactionUsersTotalPages = signal(0);
  readonly reactionUsersTotal = signal(0);
  readonly reactionUsersLoading = signal(false);
  readonly reactionUsersError = signal('');
  readonly reactionLabels: Record<PostReactionType, string> = {
    LIKE: '👍 Thích', LOVE: '❤️ Yêu thích', HAHA: '😄 Haha',
    WOW: '😮 Wow', SAD: '😢 Buồn', ANGRY: '😠 Phẫn nộ',
  };
  readonly reactionFilterOptions = computed(() => {
    const post = this.posts()[0];
    return POST_REACTION_TYPES.filter(type => (post?.reactionCounts[type] ?? 0) > 0);
  });

  readonly currentUser = this.authService.currentUser;
  readonly activeFilter = signal<CommunityFilter>('HOME');
  readonly searchTerm = signal('');
  readonly posts = signal<CommunityPost[]>([]);
  readonly postTypes = signal<PostTypeView[]>([]);
  readonly postTopics = signal<PostTopicView[]>([]);
  readonly cropTypes = signal<CropTypeView[]>([]);
  readonly cropTypesError = signal('');
  readonly currentPage = signal(0);
  readonly hasNextPage = signal(false);
  readonly feedLoading = signal(true);
  readonly loadingMore = signal(false);
  readonly feedError = signal('');
  readonly creatingPost = signal(false);
  readonly createPostError = signal('');
  readonly modalCategory = signal<PostCategory | null>(null);
  readonly modalOpen = signal(false);
  readonly announcement = signal('');

  readonly userName = computed(() => {
    const user = this.currentUser();
    return (
      user?.profile?.displayName?.trim() || user?.email?.split('@')[0] || 'Thành viên Nông Thịnh'
    );
  });
  readonly userAvatarUrl = computed(() => this.currentUser()?.profile?.avatarUrl ?? null);
  readonly userLoading = computed(() => this.currentUser() === null);
  readonly currentUserId = computed(() => this.currentUser()?.userId ?? '');
  readonly interactionsEnabled = computed(() => {
    const role = this.currentUser()?.role.replace(/^ROLE_/, '');
    return role === 'FARMER' || role === 'BRAND';
  });

  readonly filteredPosts = computed(() => {
    const query = this.searchTerm().trim().toLocaleLowerCase('vi-VN');
    if (!query || !['MINE', 'SAVED'].includes(this.activeFilter())) return this.posts();
    return this.posts().filter((post) =>
      [post.author.name, post.author.location, post.content, post.topic]
        .join(' ')
        .toLocaleLowerCase('vi-VN')
        .includes(query),
    );
  });

  readonly feedTitle = computed(() => {
    if (this.searchTerm().trim()) return `Kết quả cho “${this.searchTerm().trim()}”`;
    const labels: Record<CommunityFilter, string> = {
      HOME: 'Bảng tin hôm nay',
      MINE: 'Bài viết của tôi',
      SAVED: 'Bài viết đã lưu',
      GROUPS: 'Hoạt động từ nhóm',
      QUESTION: 'Hỏi đáp kỹ thuật',
      MARKET: 'Chợ nông sản',
      EVENTS: 'Sự kiện & mùa vụ',
    };
    return labels[this.activeFilter()];
  });

  readonly emptyDescription = computed(() => {
    if (this.activeFilter() === 'SAVED') {
      return 'Bạn chưa lưu bài viết nào. Hãy lưu những nội dung hữu ích để xem lại sau.';
    }
    if (this.activeFilter() === 'GROUPS') {
      return 'Post service chưa cung cấp API bài viết theo nhóm.';
    }
    if (this.searchTerm().trim()) {
      return 'Không tìm thấy nội dung phù hợp. Hãy thử tên cây trồng, địa phương hoặc chủ đề khác.';
    }
    if (this.activeFilter() === 'MINE') {
      return 'Bạn chưa đăng bài nào. Hãy chia sẻ câu chuyện đầu tiên với cộng đồng.';
    }
    return 'Chưa có nội dung ở mục này. Bạn có thể bắt đầu một cuộc trò chuyện mới.';
  });

  readonly mobileNavItems: MobileNavItem[] = [
    { id: 'HOME', iconUrl: '/icons/community/home.png', label: 'Trang chủ' },
    { id: 'QUESTION', iconUrl: '/icons/community/user-question.png', label: 'Hỏi đáp' },
    { id: 'GROUPS', iconUrl: '/icons/community/users-alt.png', label: 'Nhóm' },
    { id: 'SAVED', iconUrl: '/icons/community/bookmark.png', label: 'Đã lưu' },
  ];

  constructor() {
    this.realtime.events.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(event => this.applyRealtimeEvent(event));
    this.realtime.connected.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.syncVisibleMetrics());
    this.realtime.setCommunityActive(true);
    this.destroyRef.onDestroy(() => this.realtime.setCommunityActive(false));
    combineLatest([this.route.paramMap, this.route.queryParamMap])
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe(([pathParams, params]) => {
      this.focusedPostId = pathParams.get('postId') ?? params.get('postId');
      this.detailMode.set(Boolean(this.focusedPostId));
      this.searchTerm.set(this.detailMode() ? '' : params.get('q') ?? '');
      const filter = params.get('filter');
      if (filter && ['HOME', 'MINE', 'SAVED', 'GROUPS', 'QUESTION', 'MARKET', 'EVENTS'].includes(filter)) {
        this.activeFilter.set(filter as CommunityFilter);
      }
      this.reactionUsersRequestId++;
      this.reactionUsers.set([]);
      this.reactionDialogOpen.set(false);
      this.activeReactionFilter.set(null);
      this.reactionUsersPage.set(0);
      this.reactionUsersTotalPages.set(0);
      this.reactionUsersTotal.set(0);
      this.reactionUsersLoading.set(false);
      this.reactionUsersError.set('');
      if (this.catalogReady) this.loadPosts();
    });
    this.loadCatalogs();
  }

  selectFilter(filter: CommunityFilter): void {
    if (this.detailMode()) {
      void this.router.navigate(['/app/community'], { queryParams: { filter } });
      return;
    }
    if (this.activeFilter() === filter && !this.focusedPostId) return;
    this.focusedPostId = null;
    this.activeFilter.set(filter);
    if (this.catalogReady) this.loadPosts();
    if (typeof window !== 'undefined') window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  retryFeed(): void {
    this.loadCatalogs();
  }

  openPost(postId: string): void {
    void this.router.navigate(['/app/community', postId]);
  }

  showReactionUsers(postId: string): void {
    const post = this.posts().find(item => item.id === postId);
    if (!post?.reactionTotal) return;
    if (!this.detailMode()) {
      void this.router.navigate(['/app/community', postId], { fragment: 'reaction-users' });
      return;
    }
    this.reactionDialogOpen.set(true);
    this.activeReactionFilter.set(null);
    this.loadReactionUsers();
  }

  closeReactionUsers(): void {
    this.reactionDialogOpen.set(false);
    this.reactionUsersRequestId++;
    this.reactionUsersLoading.set(false);
    this.reactionUsersError.set('');
  }

  closeReactionUsersFromBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.closeReactionUsers();
  }

  selectReactionFilter(reactionType: PostReactionType | null): void {
    if (this.activeReactionFilter() === reactionType && !this.reactionUsersError()) return;
    this.activeReactionFilter.set(reactionType);
    this.loadReactionUsers(0);
  }

  @HostListener('document:keydown.escape')
  closeReactionUsersWithEscape(): void {
    if (this.reactionDialogOpen()) this.closeReactionUsers();
  }

  loadReactionUsers(page = 0): void {
    const postId = this.focusedPostId;
    if (!this.detailMode() || !postId || page < 0) return;
    const requestId = ++this.reactionUsersRequestId;
    this.reactionUsersPage.set(page);
    this.reactionUsersLoading.set(true);
    this.reactionUsersError.set('');
    this.postApi.listPostReactions(postId, page, 10, this.activeReactionFilter()).pipe(
      switchMap(response => {
        if (!response.result) throw new Error('Không thể tải danh sách cảm xúc.');
        const result = response.result;
        return this.resolveAuthors(result.items.map(item => item.actorId)).pipe(
          map(authors => ({ result, users: result.items.map(item => ({
            ...item, author: authors.get(item.actorId) ?? this.fallbackAuthor(item.actorId),
          })) })),
        );
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: ({ result, users }) => {
        if (requestId !== this.reactionUsersRequestId) return;
        if (page > 0 && !users.length) {
          this.loadReactionUsers(Math.max(0, result.totalPages - 1));
          return;
        }
        this.reactionUsers.set(users);
        this.reactionUsersPage.set(result.page);
        this.reactionUsersTotalPages.set(result.totalPages);
        this.reactionUsersTotal.set(result.totalElements);
        this.reactionUsersLoading.set(false);
      },
      error: error => {
        if (requestId !== this.reactionUsersRequestId) return;
        this.reactionUsersLoading.set(false);
        this.reactionUsersError.set(apiErrorMessage(error, 'Không thể tải danh sách cảm xúc. Vui lòng thử lại.'));
      },
    });
  }

  loadMore(): void {
    if (!this.hasNextPage() || this.loadingMore()) return;
    this.loadPosts(true);
  }

  openCreatePost(category: PostCategory | null): void {
    this.createPostError.set('');
    this.modalCategory.set(category);
    this.modalOpen.set(true);
  }

  closeCreatePost(): void {
    if (this.creatingPost()) return;
    this.modalOpen.set(false);
    this.modalCategory.set(null);
    this.createPostError.set('');
  }

  createPost(input: NewCommunityPost): void {
    if (this.creatingPost()) return;
    this.creatingPost.set(true);
    this.createPostError.set('');
    let draftId: string | null = null;

    const upload$: Observable<FileView[]> = input.mediaFiles.length
      ? forkJoin(
          input.mediaFiles.map(({ file, mediaType }) =>
            this.fileApi
              .upload(file, mediaType === 'VIDEO' ? 'POST_VIDEO' : 'POST_IMAGE')
              .pipe(
                map((response) => {
                  if (!response.result) {
                    throw new Error('File service không trả về thông tin tệp đa phương tiện.');
                  }
                  return response.result;
                }),
              ),
          ),
        )
      : of([]);

    upload$
      .pipe(
        switchMap((files) =>
          this.postApi
            .createPost({
              postTypeId: input.postTypeId,
              topicId: input.topicId,
              content: input.content,
              locationText: input.location || null,
              visibility: 'PUBLIC',
              status: 'DRAFT',
              cropTypeIds: input.cropTypeIds,
            })
            .pipe(
              map((response) => {
                if (!response.result) throw new Error('Post service không trả về bài viết đã tạo.');
                draftId = response.result.id;
                return { files, post: response.result };
              }),
            ),
        ),
        switchMap(({ files, post }) =>
          files.length
            ? from(files)
                .pipe(
                  concatMap((file, displayOrder) =>
                    this.postApi.createPostMedia(post.id, {
                      fileId: file.id,
                      displayOrder,
                      caption: null,
                    }),
                  ),
                  toArray(),
                  map(() => post),
                )
            : of(post),
        ),
        switchMap((post) => this.postApi.publishPost(post.id)),
        finalize(() => this.creatingPost.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.modalOpen.set(false);
          this.modalCategory.set(null);
          this.activeFilter.set('HOME');
          this.notify('Bài viết đã được đăng lên cộng đồng.');
          if (this.detailMode()) {
            void this.router.navigate(['/app/community']);
            return;
          }
          this.loadPosts();
        },
        error: (error) => {
          const message = apiErrorMessage(error, 'Không thể đăng bài lúc này. Vui lòng thử lại.');
          this.createPostError.set(message);
          if (!draftId) {
            return;
          }
          this.postApi
            .deletePost(draftId)
            .pipe(
              catchError(() => of(null)),
              takeUntilDestroyed(this.destroyRef),
            )
            .subscribe();
        },
      });
  }

  deletePost(postId: string): void {
    const post = this.posts().find((item) => item.id === postId);
    if (!post || post.deletePending || post.author.id !== this.currentUserId()) return;

    this.updatePost(postId, (current) => ({
      ...current,
      deletePending: true,
      deleteError: '',
    }));

    this.postApi
      .deletePost(postId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.posts.update((posts) => posts.filter((item) => item.id !== postId));
          this.notify('Đã xóa bài viết.');
          if (this.detailMode()) void this.router.navigate(['/app/community']);
        },
        error: (error) => {
          this.updatePost(postId, (current) => ({
            ...current,
            deletePending: false,
            deleteError: apiErrorMessage(error, 'Không thể xóa bài viết. Vui lòng thử lại.'),
          }));
        },
      });
  }

  changeReaction(event: CommunityReactionEvent): void {
    const post = this.posts().find((item) => item.id === event.postId);
    if (!post || post.reactionPending || !this.interactionsEnabled()) return;

    const reactionType = event.reactionType === post.currentReaction ? null : event.reactionType;
    const mutationId = ++this.reactionMutationSequence;
    const snapshot = {
      reactionVersion: post.reactionVersion,
      reactionCounts: { ...post.reactionCounts },
      reactionTotal: post.reactionTotal,
      currentReaction: post.currentReaction,
    };
    this.updatePost(event.postId, (current) => ({
      ...current,
      ...this.withReaction(current, reactionType),
      reactionPending: true,
      reactionError: '',
      reactionMutationId: mutationId,
    }));

    const mutation$: Observable<ApiResponse<Partial<ReactionMetrics>>> = reactionType
      ? this.postApi.setPostReaction(event.postId, { reactionType })
      : this.postApi.removePostReaction(event.postId);

    mutation$
      .pipe(
        switchMap((response) => {
          const result = response.result;
          if (result?.reactionCounts && result.reactionVersion !== undefined) {
            return of({
              counts: result.reactionCounts, totalCount: result.reactionTotal ?? 0,
              currentUserReaction: result.currentReaction ?? null, reactionVersion: result.reactionVersion,
            } satisfies PostReactionSummaryView);
          }
          return this.postApi.getPostReactionSummary(event.postId).pipe(
            map((response) => response.result ?? null),
            catchError(() => of(null)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (summary) => {
          this.updatePost(event.postId, (current) => {
            if (current.reactionMutationId !== mutationId) return current;
            return {
              ...current,
              ...(summary && (summary.reactionVersion ?? 0) >= current.reactionVersion
                ? { ...this.normalizedReaction(summary), reactionVersion: summary.reactionVersion ?? 0 } : {}),
              reactionPending: false,
              reactionError: '',
            };
          });
          this.flushReactionBuffer(event.postId);
          if (this.reactionDialogOpen() && this.focusedPostId === event.postId) {
            this.loadReactionUsers(this.reactionUsersPage());
          }
        },
        error: (error) => {
          this.updatePost(event.postId, (current) => {
            if (current.reactionMutationId !== mutationId) return current;
            return {
              ...current,
              ...snapshot,
              reactionPending: false,
              reactionError: apiErrorMessage(
                error,
                'Không thể cập nhật cảm xúc. Trạng thái trước đó đã được khôi phục.',
              ),
            };
          });
          this.flushReactionBuffer(event.postId);
        },
      });
  }

  toggleBookmark(postId: string): void {
    const post = this.posts().find((item) => item.id === postId);
    if (!post || post.bookmarkPending || !this.interactionsEnabled()) return;

    const wasSaved = post.saved;
    const nextSaved = !wasSaved;
    const mutationId = ++this.bookmarkMutationSequence;
    this.updatePost(postId, (current) => ({
      ...current,
      saved: nextSaved,
      bookmarkPending: true,
      bookmarkError: '',
      bookmarkMutationId: mutationId,
    }));

    const mutation$: Observable<unknown> = nextSaved
      ? this.postApi.savePostBookmark(postId)
      : this.postApi.removePostBookmark(postId);

    mutation$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.updatePost(postId, (current) =>
          current.bookmarkMutationId === mutationId
            ? {
                ...current,
                saved: nextSaved,
                bookmarkPending: false,
                bookmarkError: '',
              }
            : current,
        );
        this.notify(nextSaved ? 'Đã lưu bài viết.' : 'Đã bỏ lưu bài viết.');
        if (!nextSaved && this.activeFilter() === 'SAVED') this.loadPosts();
      },
      error: (error) => {
        this.updatePost(postId, (current) =>
          current.bookmarkMutationId === mutationId
            ? {
                ...current,
                saved: wasSaved,
                bookmarkPending: false,
                bookmarkError: apiErrorMessage(
                  error,
                  'Không thể cập nhật bài viết đã lưu. Trạng thái trước đó đã được khôi phục.',
                ),
              }
            : current,
        );
      },
    });
  }

  sharePost(postId: string): void {
    const post = this.posts().find((item) => item.id === postId);
    if (!post || post.sharePending || !this.interactionsEnabled()) return;

    const previousCount = post.shares;
    const mutationId = ++this.shareMutationSequence;
    this.updatePost(postId, (current) => ({
      ...current,
      shares: current.shares + 1,
      sharePending: true,
      shareError: '',
      shareMutationId: mutationId,
    }));

    this.postApi
      .createPostShare(postId)
      .pipe(
        switchMap(() =>
          this.postApi.getPostShareSummary(postId).pipe(
            map((response) => response.result ?? null),
            catchError(() => of(null)),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (summary) => {
          this.updatePost(postId, (current) =>
            current.shareMutationId === mutationId
              ? {
                  ...current,
                  shares: summary ? Math.max(0, Number(summary.totalCount)) : previousCount + 1,
                  sharePending: false,
                  shareError: '',
                }
              : current,
          );
          this.notify('Đã ghi nhận lượt chia sẻ bài viết.');
        },
        error: (error) => {
          this.updatePost(postId, (current) =>
            current.shareMutationId === mutationId
              ? {
                  ...current,
                  shares: previousCount,
                  sharePending: false,
                  shareError: apiErrorMessage(error, 'Không thể chia sẻ bài viết.'),
                }
              : current,
          );
        },
      });
  }

  reportPost(event: CommunityPostReportEvent): void {
    const post = this.posts().find((item) => item.id === event.postId);
    if (
      !post ||
      post.reportPending ||
      post.reported ||
      post.author.id === this.currentUserId() ||
      !this.interactionsEnabled()
    ) {
      return;
    }

    this.updatePost(event.postId, (current) => ({
      ...current,
      reportPending: true,
      reportError: '',
    }));

    this.postApi
      .createPostReport(event.postId, {
        reason: event.reason,
        reasonDetail: event.reasonDetail,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.updatePost(event.postId, (current) => ({
            ...current,
            reported: true,
            reportPending: false,
            reportError: '',
          }));
          this.notify('Đã gửi báo cáo. Quản trị viên sẽ xem xét nội dung này.');
        },
        error: (error) => {
          this.updatePost(event.postId, (current) => ({
            ...current,
            reportPending: false,
            reportError: apiErrorMessage(
              error,
              'Không thể gửi báo cáo bài viết. Vui lòng thử lại.',
            ),
          }));
        },
      });
  }

  loadComments(event: CommunityCommentsRequestEvent): void {
    const post = this.posts().find((item) => item.id === event.postId);
    if (
      !post ||
      post.commentsLoading ||
      (event.append && !post.commentsHasNext) ||
      (!event.append && post.commentsLoaded)
    ) {
      return;
    }

    const page = event.append ? post.commentsPage + 1 : 0;
    const requestId = (this.commentRequestIds.get(event.postId) ?? 0) + 1;
    const commentVersion = post.commentVersion;
    this.commentRequestIds.set(event.postId, requestId);
    this.updatePost(event.postId, (current) => ({
      ...current,
      commentsLoading: true,
      commentsError: '',
    }));

    this.postApi
      .listPostComments(event.postId, page, this.commentPageSize)
      .pipe(
        switchMap((response) => {
          const result = response.result ?? this.emptyCommentPage(page);
          return this.enrichCommentThreads(result.items).pipe(
            map((comments) => ({ result, comments })),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ result, comments }) => {
          if (this.commentRequestIds.get(event.postId) !== requestId) return;
          this.updatePost(event.postId, (current) => ({
            ...current,
            comments: event.append ? this.mergeCommentsById(current.comments, comments) : comments,
            commentRootTotal: current.commentVersion === commentVersion && !current.commentMutationPending
              ? result.totalElements : current.commentRootTotal,
            commentsPage: result.page,
            commentsHasNext: result.hasNext,
            commentsLoaded: current.commentVersion === commentVersion,
            commentsLoading: false,
            commentsError: '',
          }));
        },
        error: (error) => {
          if (this.commentRequestIds.get(event.postId) !== requestId) return;
          this.updatePost(event.postId, (current) => ({
            ...current,
            commentsLoading: false,
            commentsError: apiErrorMessage(error, 'Không thể tải bình luận. Vui lòng thử lại.'),
          }));
        },
      });
  }

  createComment(event: CommunityCommentCreateEvent): void {
    const post = this.posts().find((item) => item.id === event.postId);
    const content = event.content.trim();
    if (!post || !content || post.commentMutationPending || !this.interactionsEnabled()) return;
    this.beginCommentMutation(event.postId);

    this.postApi
      .createPostComment(event.postId, {
        content,
        parentCommentId: event.parentCommentId,
      })
      .pipe(
        switchMap((response) => {
          if (!response.result) throw new Error('Post service không trả về bình luận đã tạo.');
          this.applyCommentMetrics(response.result);
          return this.resolveAuthor(response.result.authorUserId).pipe(
            map((author) => this.toCommunityComment(response.result!, author)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (comment) => {
          this.updatePost(event.postId, (current) => ({
            ...current,
            comments: event.parentCommentId
              ? this.appendReply(current.comments, event.parentCommentId, comment)
              : [comment, ...current.comments.filter((item) => item.id !== comment.id)],
            commentRootTotal: event.parentCommentId
              ? current.commentRootTotal
              : current.commentRootTotal + 1,
            commentsLoaded: true,
            commentMutationPending: false,
            commentMutationError: '',
            commentCreateVersion: current.commentCreateVersion + 1,
          }));
          this.flushCommentBuffer(event.postId);
        },
        error: (error) => this.failCommentMutation(event.postId, error, 'Không thể gửi bình luận.'),
      });
  }

  updateComment(event: CommunityCommentUpdateEvent): void {
    const post = this.posts().find((item) => item.id === event.postId);
    const content = event.content.trim();
    if (!post || !content || post.commentMutationPending || !this.interactionsEnabled()) return;
    this.beginCommentMutation(event.postId);

    this.postApi
      .updatePostComment(event.postId, event.commentId, { content })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          if (!response.result) {
            this.failCommentMutation(
              event.postId,
              new Error('Post service không trả về bình luận đã cập nhật.'),
              'Không thể sửa bình luận.',
            );
            return;
          }
          this.updatePost(event.postId, (current) => ({
            ...current,
            comments: this.replaceComment(current.comments, response.result!),
            commentMutationPending: false,
            commentMutationError: '',
            commentEditVersion: current.commentEditVersion + 1,
          }));
          this.applyCommentMetrics(response.result);
          this.flushCommentBuffer(event.postId);
        },
        error: (error) => this.failCommentMutation(event.postId, error, 'Không thể sửa bình luận.'),
      });
  }

  deleteComment(event: CommunityCommentDeleteEvent): void {
    const post = this.posts().find((item) => item.id === event.postId);
    if (!post || post.commentMutationPending || !this.interactionsEnabled()) return;
    this.beginCommentMutation(event.postId);

    this.postApi
      .deletePostComment(event.postId, event.commentId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.updatePost(event.postId, (current) => {
            const isRoot = current.comments.some((comment) => comment.id === event.commentId);
            return {
              ...current,
              comments: this.removeComment(current.comments, event.commentId),
              commentRootTotal: isRoot
                ? Math.max(0, current.commentRootTotal - 1)
                : current.commentRootTotal,
              commentMutationPending: false,
              commentMutationError: '',
            };
          });
          if (response.result) this.applyCommentMetrics(response.result);
          this.flushCommentBuffer(event.postId);
        },
        error: (error) => this.failCommentMutation(event.postId, error, 'Không thể xóa bình luận.'),
      });
  }

  private loadCatalogs(): void {
    this.feedLoading.set(true);
    this.feedError.set('');
    this.cropTypesError.set('');
    forkJoin({
      postTypes: this.postApi.listActivePostTypes(),
      postTopics: this.postApi.listActivePostTopics(),
      cropTypes: this.agriCatalogApi.listActiveCropTypes().pipe(
        catchError((error) => {
          this.cropTypesError.set(
            apiErrorMessage(error, 'Không thể tải danh sách cây trồng. Bạn vẫn có thể đăng bài.'),
          );
          return of({ result: [] as CropTypeView[] });
        }),
      ),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ postTypes, postTopics, cropTypes }) => {
          this.postTypes.set(postTypes.result ?? []);
          this.postTopics.set(postTopics.result ?? []);
          this.cropTypes.set(cropTypes.result ?? []);
          this.catalogReady = true;
          this.loadPosts();
        },
        error: (error) => {
          this.catalogReady = true;
          this.feedError.set(
            apiErrorMessage(error, 'Không thể tải danh mục bài viết. Vui lòng thử lại.'),
          );
          this.feedLoading.set(false);
        },
      });
  }

  private loadPosts(append = false): void {
    const filter = this.activeFilter();
    if (filter === 'GROUPS' && !this.detailMode()) {
      this.feedRequestId += 1;
      this.posts.set([]);
      this.hasNextPage.set(false);
      this.feedError.set('');
      this.feedLoading.set(false);
      return;
    }

    const page = append ? this.currentPage() + 1 : 0;
    const requestId = ++this.feedRequestId;
    this.feedError.set('');
    append ? this.loadingMore.set(true) : this.feedLoading.set(true);

    const request$ = this.focusedPostId
      ? this.postApi.getPost(this.focusedPostId).pipe(map(response => {
          if (!response.result) throw new Error('Bài viết không tồn tại hoặc bạn không có quyền xem.');
          return { ...response, result: { ...this.emptyPage(0), items: [response.result], totalElements: 1 } };
        }))
      : filter === 'MINE'
        ? this.postApi.listMyPosts({ status: 'PUBLISHED', page, size: this.pageSize })
        : filter === 'SAVED'
          ? this.postApi.listMyBookmarkedPosts({ page, size: this.pageSize })
          : this.postApi.listPublicPosts({
              postTypeId: this.postTypeIdForFilter(filter),
              keyword: this.searchTerm(),
              page,
              size: this.pageSize,
            });

    request$
      .pipe(
        switchMap((response) => {
          const result = response.result ?? this.emptyPage(page);
          return this.enrichPosts(result.items, filter === 'SAVED').pipe(
            map((items) => ({ result, items })),
          );
        }),
        finalize(() => {
          if (requestId === this.feedRequestId) {
            this.feedLoading.set(false);
            this.loadingMore.set(false);
          }
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ result, items }) => {
          if (requestId !== this.feedRequestId) return;
          this.posts.update((current) => (append ? [...current, ...items] : items));
          this.syncVisibleMetrics();
          if (this.detailMode() && items.length) {
            this.loadComments({ postId: items[0].id, append: false });
            afterNextRender(() => {
              if (requestId !== this.feedRequestId) return;
              if (this.route.snapshot.fragment === 'reaction-users') {
                this.showReactionUsers(items[0].id);
              } else {
                window.scrollTo({ top: 0 });
              }
            }, { injector: this.injector });
          }
          this.currentPage.set(result.page);
          this.hasNextPage.set(result.hasNext);
        },
        error: (error) => {
          if (requestId !== this.feedRequestId) return;
          this.feedError.set(
            apiErrorMessage(error, this.detailMode()
              ? 'Bài viết không tồn tại hoặc bạn không có quyền xem.'
              : 'Không thể tải bảng tin. Vui lòng thử lại.'),
          );
        },
      });
  }

  private enrichPosts(views: PostView[], assumeBookmarked = false): Observable<CommunityPost[]> {
    if (!views.length) return of([]);
    const authorIds = [...new Set(views.map((post) => post.authorUserId))];
    return forkJoin({
      authors: this.resolveAuthors(authorIds),
      reactions: this.loadReactionEnrichments(views),
      shares: this.loadShareEnrichments(views),
      bookmarks: this.loadBookmarkEnrichments(views, assumeBookmarked),
      commentCounts: this.loadCommentCountEnrichments(views),
    }).pipe(
      map(({ authors, reactions, shares, bookmarks, commentCounts }) => {
        return views.map((post) =>
          this.toCommunityPost(
            post,
            authors.get(post.authorUserId),
            reactions.get(post.id),
            shares.get(post.id),
            bookmarks.get(post.id),
            commentCounts.get(post.id),
          ),
        );
      }),
    );
  }

  private loadReactionEnrichments(views: PostView[]): Observable<Map<string, ReactionEnrichment>> {
    return from(views).pipe(
      mergeMap(
        (post) =>
          this.postApi.getPostReactionSummary(post.id).pipe(
            map((response) => {
              const enrichment: ReactionEnrichment = response.result
                ? { summary: response.result, error: '' }
                : {
                    summary: this.emptyReactionSummary(),
                    error: 'Post service không trả về tổng hợp cảm xúc.',
                  };
              return [post.id, enrichment] as const;
            }),
            catchError((error) =>
              of([
                post.id,
                {
                  summary: this.emptyReactionSummary(),
                  error: apiErrorMessage(error, 'Không thể tải tổng hợp cảm xúc.'),
                } satisfies ReactionEnrichment,
              ] as const),
            ),
          ),
        this.maxConcurrentEnrichmentRequests,
      ),
      toArray(),
      map((entries) => new Map(entries)),
    );
  }

  private loadShareEnrichments(views: PostView[]): Observable<Map<string, ShareEnrichment>> {
    return from(views).pipe(
      mergeMap(
        (post) =>
          this.postApi.getPostShareSummary(post.id).pipe(
            map((response) => {
              const enrichment: ShareEnrichment = response.result
                ? { summary: response.result, error: '' }
                : {
                    summary: this.emptyShareSummary(post.id),
                    error: 'Post service không trả về tổng lượt chia sẻ.',
                  };
              return [post.id, enrichment] as const;
            }),
            catchError((error) =>
              of([
                post.id,
                {
                  summary: this.emptyShareSummary(post.id),
                  error: apiErrorMessage(error, 'Không thể tải tổng lượt chia sẻ.'),
                } satisfies ShareEnrichment,
              ] as const),
            ),
          ),
        this.maxConcurrentEnrichmentRequests,
      ),
      toArray(),
      map((entries) => new Map(entries)),
    );
  }

  private loadCommentCountEnrichments(views: PostView[]): Observable<Map<string, number>> {
    return from(views).pipe(
      mergeMap(
        (post) =>
          this.postApi.listPostComments(post.id, 0, 1).pipe(
            map((response) => [post.id, Math.max(0, response.result?.totalElements ?? 0)] as const),
            catchError(() => of([post.id, 0] as const)),
          ),
        this.maxConcurrentEnrichmentRequests,
      ),
      toArray(),
      map((entries) => new Map(entries)),
    );
  }

  private loadBookmarkEnrichments(
    views: PostView[],
    assumeBookmarked: boolean,
  ): Observable<Map<string, BookmarkEnrichment>> {
    if (assumeBookmarked || !this.interactionsEnabled()) {
      return of(
        new Map(
          views.map(
            (post) =>
              [
                post.id,
                {
                  status: { postId: post.id, bookmarked: assumeBookmarked },
                  error: '',
                } satisfies BookmarkEnrichment,
              ] as const,
          ),
        ),
      );
    }
    return from(views).pipe(
      mergeMap(
        (post) =>
          this.postApi.getPostBookmarkStatus(post.id).pipe(
            map((response) => {
              const enrichment: BookmarkEnrichment = response.result
                ? { status: response.result, error: '' }
                : {
                    status: { postId: post.id, bookmarked: false },
                    error: 'Post service không trả về trạng thái lưu bài viết.',
                  };
              return [post.id, enrichment] as const;
            }),
            catchError((error) =>
              of([
                post.id,
                {
                  status: { postId: post.id, bookmarked: false },
                  error: apiErrorMessage(error, 'Không thể tải trạng thái lưu bài viết.'),
                } satisfies BookmarkEnrichment,
              ] as const),
            ),
          ),
        this.maxConcurrentEnrichmentRequests,
      ),
      toArray(),
      map((entries) => new Map(entries)),
    );
  }

  private resolveAuthors(userIds: string[]): Observable<Map<string, CommunityAuthor>> {
    if (!userIds.length) return of(new Map<string, CommunityAuthor>());
    return from([...new Set(userIds)]).pipe(
      mergeMap((userId) => this.resolveAuthor(userId), this.maxConcurrentEnrichmentRequests),
      toArray(),
      map((authors) => new Map(authors.map((author) => [author.id, author]))),
    );
  }

  private resolveAuthor(userId: string): Observable<CommunityAuthor> {
    const current = this.currentUser();
    if (current?.userId === userId) return of(this.currentAuthor());
    const cached = this.authorCache.get(userId);
    if (cached) return of(cached);
    const inFlight = this.authorRequests.get(userId);
    if (inFlight) return inFlight;

    const request$ = this.profileApi.getPublicFarmerProfileByUserId(userId).pipe(
      map((response) => {
        if (!response.result) throw new Error('Farmer profile not found');
        return this.farmerAuthor(userId, response.result);
      }),
      catchError(() =>
        this.profileApi.getPublicBrandProfileByUserId(userId).pipe(
          map((response) => {
            if (!response.result) throw new Error('Brand profile not found');
            return this.brandAuthor(userId, response.result);
          }),
        ),
      ),
      catchError(() => of(this.fallbackAuthor(userId))),
      tap((author) => this.authorCache.set(userId, author)),
      finalize(() => this.authorRequests.delete(userId)),
      shareReplay({ bufferSize: 1, refCount: false }),
    );
    this.authorRequests.set(userId, request$);
    return request$;
  }

  private toCommunityPost(
    view: PostView,
    baseAuthor?: CommunityAuthor,
    reaction?: ReactionEnrichment,
    share?: ShareEnrichment,
    bookmark?: BookmarkEnrichment,
    commentRootTotal = 0,
  ): CommunityPost {
    const type = this.postTypes().find((item) => item.id === view.postTypeId);
    const topic = this.postTopics().find((item) => item.id === view.topicId);
    const author = baseAuthor ?? this.fallbackAuthor(view.authorUserId);
    const normalizedReaction = this.normalizedReaction(
      reaction?.summary ?? this.emptyReactionSummary(),
    );
    return {
      id: view.id,
      author: { ...author, location: view.locationText?.trim() || author.location },
      content: view.content,
      category: type?.code.trim().toUpperCase() || null,
      categoryLabel: type?.name.trim() || undefined,
      topic: topic?.name.trim() || '',
      createdAt: this.relativeTime(view.publishedAt ?? view.createdAt),
      imageUrls: view.media
        .filter((item) => item.mediaType === 'IMAGE')
        .map((item) => item.mediaUrl),
      videoUrl: view.media.find((item) => item.mediaType === 'VIDEO')?.mediaUrl ?? null,
      ...normalizedReaction,
      reactionPending: false,
      reactionError: reaction?.error ?? '',
      reactionMutationId: 0,
      reactionVersion: reaction?.summary.reactionVersion ?? 0,
      commentVersion: 0,
      shares: Math.max(0, Number(share?.summary.totalCount ?? 0)),
      sharePending: false,
      shareError: share?.error ?? '',
      shareMutationId: 0,
      reported: false,
      reportPending: false,
      reportError: '',
      deletePending: false,
      deleteError: '',
      saved: bookmark?.status.bookmarked ?? false,
      bookmarkPending: false,
      bookmarkError: bookmark?.error ?? '',
      bookmarkMutationId: 0,
      comments: [],
      commentRootTotal,
      commentsPage: 0,
      commentsHasNext: false,
      commentsLoaded: false,
      commentsLoading: false,
      commentsError: '',
      commentMutationPending: false,
      commentMutationError: '',
      commentCreateVersion: 0,
      commentEditVersion: 0,
    };
  }

  private enrichCommentThreads(threads: PostCommentThreadView[]): Observable<CommunityComment[]> {
    const views = threads.flatMap((thread) => [thread.comment, ...thread.replies]);
    return this.resolveAuthors(views.map((comment) => comment.authorUserId)).pipe(
      map((authors) =>
        threads.map((thread) =>
          this.toCommunityComment(
            thread.comment,
            authors.get(thread.comment.authorUserId),
            thread.replies.map((reply) =>
              this.toCommunityComment(reply, authors.get(reply.authorUserId)),
            ),
          ),
        ),
      ),
    );
  }

  private toCommunityComment(
    view: PostCommentView,
    author?: CommunityAuthor,
    replies: CommunityComment[] = [],
  ): CommunityComment {
    return {
      id: view.id,
      parentCommentId: view.parentCommentId,
      author: author ?? this.fallbackAuthor(view.authorUserId),
      content: view.content,
      createdAt: this.relativeTime(view.createdAt),
      updatedAt: view.updatedAt,
      edited: view.updatedAt !== view.createdAt,
      replies,
    };
  }

  private normalizedReaction(
    summary: PostReactionSummaryView,
  ): Pick<CommunityPost, 'reactionCounts' | 'reactionTotal' | 'currentReaction'> {
    const reactionCounts = this.emptyReactionCounts();
    for (const type of POST_REACTION_TYPES) {
      reactionCounts[type] = Math.max(0, Number(summary.counts?.[type] ?? 0));
    }
    return {
      reactionCounts,
      reactionTotal: POST_REACTION_TYPES.reduce((total, type) => total + reactionCounts[type], 0),
      currentReaction: summary.currentUserReaction ?? null,
    };
  }

  private withReaction(
    post: CommunityPost,
    reactionType: PostReactionType | null,
  ): Pick<CommunityPost, 'reactionCounts' | 'reactionTotal' | 'currentReaction'> {
    const reactionCounts = { ...post.reactionCounts };
    if (post.currentReaction) {
      reactionCounts[post.currentReaction] = Math.max(0, reactionCounts[post.currentReaction] - 1);
    }
    if (reactionType) reactionCounts[reactionType] += 1;
    return {
      reactionCounts,
      reactionTotal: POST_REACTION_TYPES.reduce((total, type) => total + reactionCounts[type], 0),
      currentReaction: reactionType,
    };
  }

  private emptyReactionSummary(): PostReactionSummaryView {
    return {
      totalCount: 0,
      counts: this.emptyReactionCounts(),
      currentUserReaction: null,
    };
  }

  private emptyShareSummary(postId: string): PostShareSummaryView {
    return { postId, totalCount: 0 };
  }

  private emptyReactionCounts(): Record<PostReactionType, number> {
    return { LIKE: 0, LOVE: 0, HAHA: 0, WOW: 0, SAD: 0, ANGRY: 0 };
  }

  private mergeCommentsById(
    current: CommunityComment[],
    incoming: CommunityComment[],
  ): CommunityComment[] {
    const merged = [...current];
    const ids = new Set(current.map((comment) => comment.id));
    for (const comment of incoming) {
      if (!ids.has(comment.id)) {
        merged.push(comment);
        ids.add(comment.id);
      }
    }
    return merged;
  }

  private appendReply(
    comments: CommunityComment[],
    parentCommentId: string,
    reply: CommunityComment,
  ): CommunityComment[] {
    return comments.map((comment) =>
      comment.id === parentCommentId
        ? {
            ...comment,
            replies: [...comment.replies.filter((item) => item.id !== reply.id), reply],
          }
        : comment,
    );
  }

  private replaceComment(
    comments: CommunityComment[],
    updated: PostCommentView,
  ): CommunityComment[] {
    return comments.map((comment) => {
      if (comment.id === updated.id) {
        return this.toCommunityComment(updated, comment.author, comment.replies);
      }
      return {
        ...comment,
        replies: comment.replies.map((reply) =>
          reply.id === updated.id
            ? this.toCommunityComment(updated, reply.author, reply.replies)
            : reply,
        ),
      };
    });
  }

  private removeComment(comments: CommunityComment[], commentId: string): CommunityComment[] {
    return comments
      .filter((comment) => comment.id !== commentId)
      .map((comment) => ({
        ...comment,
        replies: comment.replies.filter((reply) => reply.id !== commentId),
      }));
  }

  private beginCommentMutation(postId: string): void {
    this.commentRequestIds.set(postId, (this.commentRequestIds.get(postId) ?? 0) + 1);
    this.updatePost(postId, (current) => ({
      ...current,
      commentsLoading: false,
      commentMutationPending: true,
      commentMutationError: '',
    }));
  }

  private failCommentMutation(postId: string, error: unknown, fallback: string): void {
    this.updatePost(postId, (current) => ({
      ...current,
      commentMutationPending: false,
      commentMutationError: apiErrorMessage(error, fallback),
    }));
    this.flushCommentBuffer(postId);
  }

  private applyRealtimeEvent(event: RealtimeEvent): void {
    if (!event.postId || !this.posts().some(post => post.id === event.postId)) return;
    if (event.eventType === 'post.reaction.updated') {
      this.applyReactionMetrics({ ...event.payload, postId: event.postId, reactionVersion: event.version } as ReactionMetrics);
    } else if (event.eventType === 'post.comment.updated') {
      this.applyCommentMetrics({ ...event.payload, postId: event.postId, commentVersion: event.version });
    }
  }

  private applyReactionMetrics(metrics: ReactionMetrics): void {
    if (!metrics.reactionCounts || !Number.isSafeInteger(metrics.reactionVersion)) return;
    this.updatePost(metrics.postId, post => {
      if (metrics.reactionVersion < post.reactionVersion
          || (metrics.reactionVersion === post.reactionVersion && metrics.currentReaction === undefined)) return post;
      if (post.reactionPending) {
        if (metrics.reactionVersion > (this.reactionBuffer.get(post.id)?.reactionVersion ?? -1))
          this.reactionBuffer.set(post.id, metrics);
        return post;
      }
      return { ...post, reactionCounts: { ...metrics.reactionCounts }, reactionTotal: metrics.reactionTotal,
        reactionVersion: metrics.reactionVersion,
        ...(metrics.currentReaction !== undefined ? { currentReaction: metrics.currentReaction } : {}) };
    });
  }

  private applyCommentMetrics(metrics: Partial<CommentMetrics>): void {
    if (!metrics.postId || metrics.commentVersion === undefined || metrics.commentRootTotal === undefined
        || !Number.isSafeInteger(metrics.commentVersion)) return;
    this.updatePost(metrics.postId, post => {
      if (metrics.commentVersion! <= post.commentVersion) return post;
      if (post.commentMutationPending) {
        if (metrics.commentVersion! > (this.commentBuffer.get(post.id)?.commentVersion ?? -1))
          this.commentBuffer.set(post.id, metrics as CommentMetrics);
        return post;
      }
      return { ...post, commentRootTotal: metrics.commentRootTotal!, commentVersion: metrics.commentVersion!,
        commentsLoaded: false };
    });
  }

  private flushReactionBuffer(postId: string): void {
    const metrics = this.reactionBuffer.get(postId);
    this.reactionBuffer.delete(postId);
    if (metrics) this.applyReactionMetrics(metrics);
  }

  private flushCommentBuffer(postId: string): void {
    const metrics = this.commentBuffer.get(postId);
    this.commentBuffer.delete(postId);
    if (metrics) this.applyCommentMetrics(metrics);
  }

  private syncVisibleMetrics(): void {
    if (!this.realtime.features().postEngagement) return;
    from(this.posts().map(post => post.id)).pipe(
      mergeMap(postId => this.postApi.getPostEngagement(postId).pipe(
        map(response => response.result), catchError(() => of(null)),
      ), this.maxConcurrentEnrichmentRequests),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(metrics => {
      if (!metrics) return;
      this.applyReactionMetrics(metrics);
      this.applyCommentMetrics(metrics);
    });
  }

  private updatePost(postId: string, update: (post: CommunityPost) => CommunityPost): void {
    this.posts.update((posts) => posts.map((post) => (post.id === postId ? update(post) : post)));
  }

  private postTypeIdForFilter(filter: CommunityFilter): string | null {
    const code =
      filter === 'QUESTION'
        ? 'QUESTION'
        : filter === 'MARKET'
          ? this.postTypes().some((item) => item.code === 'MARKET')
            ? 'MARKET'
            : 'PRICE'
          : filter === 'EVENTS'
            ? this.postTypes().some((item) => item.code === 'EVENT')
              ? 'EVENT'
              : 'WARNING'
            : null;
    return code ? (this.postTypes().find((item) => item.code === code)?.id ?? null) : null;
  }

  private farmerAuthor(userId: string, profile: FarmerProfilePublicResponse): CommunityAuthor {
    return {
      id: userId,
      name:
        [profile.firstName, profile.lastName].filter(Boolean).join(' ') || 'Nông dân Nông Thịnh',
      avatarUrl: profile.avatarUrl,
      location: profile.provinceId?.trim() ?? '',
      roleLabel: 'Nông dân',
      verified: false,
    };
  }

  private brandAuthor(userId: string, profile: BrandProfilePublicResponse): CommunityAuthor {
    return {
      id: userId,
      name: profile.brandName || 'Thương hiệu Nông Thịnh',
      avatarUrl: profile.logoUrl,
      location: profile.officeProvinceId?.trim() ?? '',
      roleLabel: 'Thương hiệu',
      verified: true,
    };
  }

  private fallbackAuthor(userId: string): CommunityAuthor {
    return {
      id: userId,
      name: `Thành viên ${userId.slice(0, 8)}`,
      avatarUrl: null,
      location: '',
    };
  }

  private currentAuthor(): CommunityAuthor {
    const user = this.currentUser();
    return {
      id: user?.userId ?? 'current-user',
      name: this.userName(),
      avatarUrl: this.userAvatarUrl(),
      location: '',
      verified: user?.role === 'ROLE_BRAND' || user?.role === 'BRAND',
    };
  }

  private relativeTime(value: string): string {
    const timestamp = new Date(value).getTime();
    if (!Number.isFinite(timestamp)) return 'Vừa xong';
    const seconds = Math.round((timestamp - Date.now()) / 1000);
    const formatter = new Intl.RelativeTimeFormat('vi', { numeric: 'auto' });
    const ranges: Array<[number, Intl.RelativeTimeFormatUnit]> = [
      [60, 'second'],
      [60, 'minute'],
      [24, 'hour'],
      [7, 'day'],
      [4.345, 'week'],
      [12, 'month'],
      [Number.POSITIVE_INFINITY, 'year'],
    ];
    let duration = seconds;
    for (const [amount, unit] of ranges) {
      if (Math.abs(duration) < amount) return formatter.format(Math.round(duration), unit);
      duration /= amount;
    }
    return 'Vừa xong';
  }

  private emptyPage(page: number): PageView<PostView> {
    return {
      items: [],
      page,
      size: this.pageSize,
      totalElements: 0,
      totalPages: 0,
      hasNext: false,
    };
  }

  private emptyCommentPage(page: number): PageView<PostCommentThreadView> {
    return {
      items: [],
      page,
      size: this.commentPageSize,
      totalElements: 0,
      totalPages: 0,
      hasNext: false,
    };
  }

  private notify(message: string): void {
    this.announcement.set(message);
    timer(3200)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        if (this.announcement() === message) this.announcement.set('');
      });
  }
}
