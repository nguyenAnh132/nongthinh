import { provideUploadPolicyFixtures } from '../../../core/service/upload-policy.testing';
import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { BehaviorSubject, Subject, of, throwError } from 'rxjs';
import { AgriCatalogApiService, CropTypeView } from '../../../core/api/agri-catalog-api.service';
import { FileApiService } from '../../../core/api/file-api.service';
import {
  PostApiService,
  PostCommentThreadView,
  PostCommentView,
  PostReactionSummaryView,
  PostView,
} from '../../../core/api/post-api.service';
import { ProfileApiService } from '../../../core/api/profile-api.service';
import { FollowApiService } from '../../../core/api/follow-api.service';
import { AuthService } from '../../../core/auth/auth.service';
import { RealtimeService } from '../../../core/realtime/realtime.service';
import { RealtimeEvent } from '../../../core/realtime/realtime.models';
import { Community } from './community';
import { PostCard } from './components/post-card/post-card';

describe('Community interaction state', () => {
  const realtimeEvents = new Subject<RealtimeEvent>();
  const connected = new Subject<void>();
  let reactionMutation: Subject<unknown>;
  let getPostEngagement: ReturnType<typeof vi.fn>;

  it('buffers comment metrics during a pending reply without incrementing the root count', () => {
    const pending = new Subject<unknown>();
    createPostComment.mockReturnValue(pending);
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.createComment({ postId: 'post-1', parentCommentId: 'comment-1', content: 'Reply' });
    realtimeEvents.next({
      eventId: 'comment-7', eventType: 'post.comment.updated', schemaVersion: 1,
      occurredAt: '2026-09-09T00:00:00Z', postId: 'post-1', version: 7,
      payload: { postId: 'post-1', commentVersion: 7, commentRootTotal: 8, commentTotal: 12 },
    });
    expect(component.posts()[0].commentRootTotal).toBe(1);
    pending.next({ result: { ...commentView('reply-2', 'comment-1'), commentRootTotal: 1, commentTotal: 3, commentVersion: 6 } });
    pending.complete();
    expect(component.posts()[0]).toEqual(expect.objectContaining({
      commentRootTotal: 8, commentVersion: 7, commentMutationPending: false,
    }));
  });

  it('restores absolute metrics and the users reaction on reconnect even at the same version', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    emitReaction(10, 30);
    getPostEngagement.mockReturnValue(of({ result: {
      postId: 'post-1', reactionVersion: 10, reactionTotal: 30, currentReaction: 'LOVE',
      reactionCounts: { LIKE: 29, LOVE: 1, HAHA: 0, WOW: 0, SAD: 0, ANGRY: 0 },
      commentRootTotal: 12, commentTotal: 20, commentVersion: 11,
    } }));
    const realtime = TestBed.inject(RealtimeService);
    realtime.features.set({ notifications: true, postEngagement: true });
    connected.next();
    expect(getPostEngagement).toHaveBeenCalledWith('post-1');
    expect(fixture.componentInstance.posts()[0]).toEqual(expect.objectContaining({
      reactionVersion: 10, reactionTotal: 30, currentReaction: 'LOVE', commentRootTotal: 12, commentVersion: 11,
    }));
  });

  it('ignores an old REST response after the feed was reloaded and a newer mutation completed', () => {
    const older = new Subject<unknown>();
    const newer = new Subject<unknown>();
    setPostReaction.mockReturnValueOnce(older).mockReturnValueOnce(newer);
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.changeReaction({ postId: 'post-1', reactionType: 'LOVE' });
    component.selectFilter('MINE');
    component.changeReaction({ postId: 'post-1', reactionType: 'WOW' });
    const result = { postId: 'post-1', reactionVersion: 12, reactionTotal: 3, currentReaction: 'WOW',
      reactionCounts: { LIKE: 2, LOVE: 0, HAHA: 0, WOW: 1, SAD: 0, ANGRY: 0 } };
    newer.next({ result });
    newer.complete();
    older.next({ result: { ...result, reactionVersion: 11, currentReaction: 'LOVE' } });
    older.complete();
    expect(component.posts()[0]).toEqual(expect.objectContaining({ reactionVersion: 12, currentReaction: 'WOW' }));
  });
  function emitReaction(version: number, total: number): void {
    realtimeEvents.next({
      eventId: 'event-' + version, eventType: 'post.reaction.updated', schemaVersion: 1,
      occurredAt: '2026-09-09T00:00:00Z', postId: 'post-1', version,
      payload: { postId: 'post-1', reactionVersion: version, reactionTotal: total,
        reactionCounts: { LIKE: total, LOVE: 0, HAHA: 0, WOW: 0, SAD: 0, ANGRY: 0 } },
    });
  }

  it('ignores duplicate and older SSE without changing the current users reaction', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    const original = fixture.componentInstance.posts()[0].currentReaction;
    emitReaction(4, 20);
    emitReaction(4, 999);
    emitReaction(3, 999);
    expect(fixture.componentInstance.posts()[0]).toEqual(expect.objectContaining({
      reactionVersion: 4, reactionTotal: 20, currentReaction: original,
    }));
  });

  it('buffers SSE while pending and selects the latest counts after REST success', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.changeReaction({ postId: 'post-1', reactionType: 'LOVE' });
    const optimisticTotal = component.posts()[0].reactionTotal;
    emitReaction(9, 30);
    expect(component.posts()[0].reactionTotal).toBe(optimisticTotal);
    reactionMutation.next({ result: {
      postId: 'post-1', currentReaction: 'LOVE', reactionVersion: 8, reactionTotal: 10,
      reactionCounts: { LIKE: 9, LOVE: 1, HAHA: 0, WOW: 0, SAD: 0, ANGRY: 0 },
    } });
    reactionMutation.complete();
    expect(component.posts()[0]).toEqual(expect.objectContaining({
      reactionVersion: 9, reactionTotal: 30, currentReaction: 'LOVE', reactionPending: false,
    }));
  });

  it('rolls back the users choice then applies buffered SSE when REST fails', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    const original = component.posts()[0].currentReaction;
    component.changeReaction({ postId: 'post-1', reactionType: 'LOVE' });
    emitReaction(6, 22);
    reactionMutation.error(new Error('Offline'));
    expect(component.posts()[0]).toEqual(expect.objectContaining({
      reactionVersion: 6, reactionTotal: 22, currentReaction: original, reactionPending: false,
    }));
  });

  it('keeps comment versions independent from reaction versions', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    emitReaction(100, 20);
    for (const [version, total] of [[3, 8], [2, 100], [3, 100]]) {
      realtimeEvents.next({
        eventId: 'comment-' + version, eventType: 'post.comment.updated', schemaVersion: 1,
        occurredAt: '2026-09-09T00:00:00Z', postId: 'post-1', version,
        payload: { postId: 'post-1', commentVersion: version, commentRootTotal: total, commentTotal: total + 2 },
      });
    }
    expect(fixture.componentInstance.posts()[0]).toEqual(expect.objectContaining({
      reactionVersion: 100, commentVersion: 3, commentRootTotal: 8,
    }));
  });
  let setPostReaction: ReturnType<typeof vi.fn>;
  let createPostComment: ReturnType<typeof vi.fn>;
  let updatePostComment: ReturnType<typeof vi.fn>;
  let deletePostComment: ReturnType<typeof vi.fn>;
  let listActiveCropTypes: ReturnType<typeof vi.fn>;
  let listPublicPosts: ReturnType<typeof vi.fn>;
  let listMyBookmarkedPosts: ReturnType<typeof vi.fn>;
  let getPostBookmarkStatus: ReturnType<typeof vi.fn>;
  let savePostBookmark: ReturnType<typeof vi.fn>;
  let removePostBookmark: ReturnType<typeof vi.fn>;
  let getPostShareSummary: ReturnType<typeof vi.fn>;
  let createPostShare: ReturnType<typeof vi.fn>;
  let createPostReport: ReturnType<typeof vi.fn>;
  let deletePost: ReturnType<typeof vi.fn>;
  let listPostComments: ReturnType<typeof vi.fn>;
  let upload: ReturnType<typeof vi.fn>;
  let createPost: ReturnType<typeof vi.fn>;
  let createPostMedia: ReturnType<typeof vi.fn>;
  let publishPost: ReturnType<typeof vi.fn>;
  let routeParams: BehaviorSubject<ReturnType<typeof convertToParamMap>>;
  let getPost: ReturnType<typeof vi.fn>;
  let listPostReactions: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    vi.spyOn(window, 'scrollTo').mockImplementation(() => {});
    routeParams = new BehaviorSubject(convertToParamMap({}));
    getPost = vi.fn(() => of({ result: postView() }));
    listPostReactions = vi.fn(() => of({ result: {
      items: [{ id: 'reaction-1', actorId: 'user-1', postId: 'post-1', reactionType: 'LOVE' }],
      page: 0, size: 10, totalElements: 11, totalPages: 2, hasNext: true,
    } }));
    reactionMutation = new Subject<unknown>();
    getPostEngagement = vi.fn(() => of({ result: null }));
    setPostReaction = vi.fn(() => reactionMutation);
    createPostComment = vi.fn(() => of({ result: commentView('reply-new', 'comment-1') }));
    updatePostComment = vi.fn((_postId: string, commentId: string) =>
      of({
        result: {
          ...commentView(commentId, null),
          content: 'Nội dung đã sửa',
          updatedAt: '2026-08-24T02:00:00Z',
        },
      }),
    );
    deletePostComment = vi.fn(() => of({ result: undefined }));
    listActiveCropTypes = vi.fn(() => of({ result: [cropType()] }));
    listPublicPosts = vi.fn(() => of({ result: postPage() }));
    listMyBookmarkedPosts = vi.fn(() => of({ result: postPage() }));
    getPostBookmarkStatus = vi.fn(() => of({ result: { postId: 'post-1', bookmarked: false } }));
    savePostBookmark = vi.fn(() =>
      of({ result: { postId: 'post-1', userId: 'user-1', createdAt: '2026-08-24T00:00:00Z' } }),
    );
    removePostBookmark = vi.fn(() => of({ result: undefined }));
    getPostShareSummary = vi
      .fn()
      .mockReturnValueOnce(of({ result: { postId: 'post-1', totalCount: 3 } }))
      .mockReturnValue(of({ result: { postId: 'post-1', totalCount: 4 } }));
    createPostShare = vi.fn(() =>
      of({
        result: {
          id: 'share-1',
          postId: 'post-1',
          sharedByUserId: 'user-1',
          createdAt: '2026-08-24T00:00:00Z',
        },
      }),
    );
    createPostReport = vi.fn(() =>
      of({
        result: {
          id: 'report-1',
          postId: 'post-1',
          reporterId: 'user-1',
          reason: 'MISINFORMATION',
          reasonDetail: 'Thông tin chưa chính xác',
          status: 'PENDING',
        },
      }),
    );
    deletePost = vi.fn(() => of({ result: undefined }));
    listPostComments = vi.fn(() => of({ result: commentPage() }));
    upload = vi.fn((file: File) =>
      of({ result: fileView(file.name === 'one.jpg' ? 'file-1' : 'file-2', file.name) }),
    );
    createPost = vi.fn(() =>
      of({ result: { ...postView(), id: 'draft-1', status: 'DRAFT', publishedAt: null } }),
    );
    createPostMedia = vi.fn(() => of({ result: {} }));
    publishPost = vi.fn(() => of({ result: postView() }));

    await TestBed.configureTestingModule({
      imports: [Community],
      providers: [
        provideUploadPolicyFixtures(),
        provideRouter([]),
        { provide: FollowApiService, useValue: {
          following: signal({}), pending: signal({}), statuses: vi.fn(() => of({ result: [] })),
        } },
        { provide: RealtimeService, useValue: {
          events: realtimeEvents, connected, setCommunityActive: vi.fn(),
          features: signal({ notifications: false, postEngagement: false }),
        } },
        {
          provide: AuthService,
          useValue: {
            currentUser: signal({
              userId: 'user-1',
              email: 'farmer@example.com',
              role: 'ROLE_FARMER',
              profile: { displayName: 'Nguyễn Văn A', avatarUrl: null },
            }).asReadonly(),
          },
        },
        {
          provide: PostApiService,
          useValue: {
            getPost,
            listPostReactions,
            listActivePostTypes: vi.fn(() => of({ result: [postType()] })),
            listActivePostTopics: vi.fn(() => of({ result: [] })),
            listTrendingPostTopics: vi.fn(() => of({ result: [] })),
            listPublicPosts,
            listMyPosts: vi.fn(() => of({ result: postPage() })),
            listMyBookmarkedPosts,
            getPostReactionSummary: vi.fn(() => of({ result: reactionSummary() })),
            getPostEngagement,
            setPostReaction,
            removePostReaction: vi.fn(() => of({ result: undefined })),
            getPostBookmarkStatus,
            savePostBookmark,
            removePostBookmark,
            getPostShareSummary,
            createPostShare,
            createPostReport,
            listPostComments,
            createPostComment,
            updatePostComment,
            deletePostComment,
            createPost,
            createPostMedia,
            publishPost,
            deletePost,
          },
        },
        {
          provide: AgriCatalogApiService,
          useValue: { listActiveCropTypes },
        },
        {
          provide: FileApiService,
          useValue: { upload },
        },
        {
          provide: ProfileApiService,
          useValue: {
            getPublicFarmerProfileByUserId: vi.fn(),
            getPublicBrandProfileByUserId: vi.fn(),
          },
        },
        {
          provide: ActivatedRoute,
          useValue: { paramMap: routeParams, queryParamMap: of(convertToParamMap({})), snapshot: { fragment: null } },
        },
      ],
    }).compileComponents();
  });

  it('opens a post from the feed without navigating when an interaction button is clicked', () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.reaction-trigger').click();
    expect(navigate).not.toHaveBeenCalled();
    fixture.nativeElement.querySelector('.post-content-text').click();
    expect(navigate).toHaveBeenCalledWith(['/app/community', 'post-1']);
  });

  it('filters an embedded profile feed by author and keeps post interactions enabled', async () => {
    const fixture = TestBed.createComponent(Community);
    listPublicPosts.mockClear();
    fixture.componentRef.setInput('embedded', true);
    fixture.componentRef.setInput('authorUserId', 'user-1');
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(listPublicPosts).toHaveBeenLastCalledWith(expect.objectContaining({
      authorUserId: 'user-1',
      page: 0,
      size: 20,
    }));
    const ownPost = fixture.componentInstance.posts()[0];
    fixture.componentInstance.posts.set([
      ownPost,
      { ...ownPost, id: 'other-post', author: { ...ownPost.author, id: 'other-user' } },
    ]);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-community-sidebar')).toBeNull();
    expect(fixture.nativeElement.querySelectorAll('.post-card')).toHaveLength(1);
    expect((fixture.nativeElement.querySelector('.reaction-trigger') as HTMLButtonElement).disabled).toBe(false);
    expect((fixture.nativeElement.querySelector('.share-action') as HTMLButtonElement).disabled).toBe(false);
    expect((fixture.nativeElement.querySelector('.save-action') as HTMLButtonElement).disabled).toBe(false);
  });

  it('loads a detail URL and only opens paginated reaction users after clicking the count', () => {
    routeParams.next(convertToParamMap({ postId: 'post-1' }));
    listPostComments.mockImplementation((_postId: string, page: number) => of({ result: {
      ...commentPage(), page, totalElements: 11, totalPages: 2, hasNext: page === 0,
      items: page === 0 ? commentPage().items : [{ comment: commentView('comment-2', null), replies: [] }],
    } }));
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    expect(getPost).toHaveBeenCalledWith('post-1');
    expect(listPublicPosts).not.toHaveBeenCalled();
    expect(listPostComments).toHaveBeenCalledWith('post-1', 0, 10);
    expect(listPostReactions).not.toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('.comments')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.reaction-dialog')).toBeNull();
    expect(fixture.nativeElement.querySelector('app-create-post-card')).toBeNull();
    expect(fixture.nativeElement.querySelector('.feed-intro')).toBeNull();
    expect(fixture.nativeElement.querySelector('.back-to-community').textContent).toContain('Quay lại');
    expect(fixture.nativeElement.querySelector('.back-to-community img').getAttribute('src'))
      .toBe('/icons/business/left.png');
    fixture.nativeElement.querySelector('.reaction-summary').click();
    fixture.detectChanges();
    expect(listPostReactions).toHaveBeenCalledWith('post-1', 0, 10, null);
    expect(fixture.nativeElement.querySelector('.reaction-dialog').textContent).toContain('Nguyễn Văn A');
    fixture.componentInstance.loadComments({ postId: 'post-1', append: true });
    expect(listPostComments).toHaveBeenCalledWith('post-1', 1, 10);
    expect(fixture.componentInstance.posts()[0].comments.map(comment => comment.id)).toEqual(['comment-1', 'comment-2']);
    expect(fixture.componentInstance.posts()[0].commentsHasNext).toBe(false);
    fixture.componentInstance.loadReactionUsers(1);
    expect(listPostReactions).toHaveBeenCalledWith('post-1', 1, 10, null);
  });

  it('renders empty comments and reactions and retries a failed comment request', () => {
    routeParams.next(convertToParamMap({ postId: 'post-1' }));
    listPostComments.mockReturnValueOnce(of({ result: commentPage() }))
      .mockReturnValueOnce(throwError(() => new Error('Offline')))
      .mockReturnValue(of({ result: { ...commentPage(), items: [], totalElements: 0, totalPages: 0 } }));
    listPostReactions.mockReturnValue(of({ result: { items: [], page: 0, totalElements: 0, totalPages: 0, hasNext: false } }));
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    expect(fixture.componentInstance.posts()[0].commentsError).toBeTruthy();
    fixture.componentInstance.loadComments({ postId: 'post-1', append: false });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.comments').textContent).toContain('Chưa có bình luận');
    fixture.nativeElement.querySelector('.reaction-summary').click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.reaction-dialog').textContent).toContain('Chưa có ai bày tỏ cảm xúc');
  });

  it('keeps the post and comments visible when listing reactions fails and supports retry', () => {
    routeParams.next(convertToParamMap({ postId: 'post-1' }));
    listPostReactions.mockReturnValueOnce(throwError(() => new Error('Offline')));
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.reaction-summary').click();
    fixture.detectChanges();
    expect(fixture.componentInstance.reactionUsersError()).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.post-card')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.comments')).not.toBeNull();
    fixture.componentInstance.loadReactionUsers();
    expect(fixture.componentInstance.reactionUsersError()).toBe('');
    expect(fixture.componentInstance.reactionUsers()).toHaveLength(1);
  });

  it('filters reaction users by type and closes the dialog with Escape', () => {
    routeParams.next(convertToParamMap({ postId: 'post-1' }));
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.reaction-summary').click();
    fixture.detectChanges();
    const likeTab = Array.from(fixture.nativeElement.querySelectorAll('.reaction-dialog__tabs button'))
      .find((button: unknown) => (button as HTMLButtonElement).textContent?.includes('Thích')) as HTMLButtonElement;
    likeTab.click();
    fixture.detectChanges();
    expect(listPostReactions).toHaveBeenLastCalledWith('post-1', 0, 10, 'LIKE');
    expect(fixture.componentInstance.activeReactionFilter()).toBe('LIKE');
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.reaction-dialog')).toBeNull();
  });

  it('does not apply stale reaction users after navigating to another post', () => {
    routeParams.next(convertToParamMap({ postId: 'post-1' }));
    const pending = new Subject<unknown>();
    listPostReactions.mockReturnValueOnce(pending);
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.reaction-summary').click();
    getPost.mockReturnValue(of({ result: { ...postView(), id: 'post-2' } }));
    routeParams.next(convertToParamMap({ postId: 'post-2' }));
    pending.next({ result: { items: [], page: 0, totalElements: 0, totalPages: 0 } });
    pending.complete();
    expect(fixture.componentInstance.posts()[0].id).toBe('post-2');
    expect(fixture.componentInstance.reactionUsers()).toHaveLength(0);
  });

  it('shows an error for unavailable posts without loading their reaction users', () => {
    routeParams.next(convertToParamMap({ postId: 'missing-post' }));
    getPost.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 404 })));
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    expect(fixture.componentInstance.feedError()).toBeTruthy();
    expect(listPostReactions).not.toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('.back-to-community')).not.toBeNull();
  });

  it('loads active crop types for the create-post form', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();

    expect(listActiveCropTypes).toHaveBeenCalledOnce();
    expect(fixture.componentInstance.cropTypes()).toEqual([cropType()]);
  });

  it('uploads and attaches multiple images in their selected order', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    const first = new File(['one'], 'one.jpg', { type: 'image/jpeg' });
    const second = new File(['two'], 'two.jpg', { type: 'image/jpeg' });

    fixture.componentInstance.createPost({
      content: 'Bài viết có nhiều ảnh',
      postTypeId: null,
      topicId: null,
      location: '',
      cropTypeIds: [],
      mediaFiles: [
        { file: first, mediaType: 'IMAGE' },
        { file: second, mediaType: 'IMAGE' },
      ],
    });

    expect(upload).toHaveBeenNthCalledWith(1, first, 'POST_IMAGE');
    expect(upload).toHaveBeenNthCalledWith(2, second, 'POST_IMAGE');
    expect(createPostMedia).toHaveBeenNthCalledWith(1, 'draft-1', {
      fileId: 'file-1',
      displayOrder: 0,
      caption: null,
    });
    expect(createPostMedia).toHaveBeenNthCalledWith(2, 'draft-1', {
      fileId: 'file-2',
      displayOrder: 1,
      caption: null,
    });
    expect(publishPost).toHaveBeenCalledWith('draft-1');
  });

  it('maps each post type by id and keeps the backend type name for display', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();

    expect(fixture.componentInstance.posts()[0]).toEqual(
      expect.objectContaining({
        category: 'EXPERIENCE',
        categoryLabel: 'Kinh nghiệm',
        topic: '',
      }),
    );
    expect(fixture.nativeElement.querySelector('.category-tag')?.textContent).toContain(
      'Kinh nghiệm',
    );
    expect(fixture.nativeElement.querySelector('.category-tag')?.textContent).not.toContain(
      'Hỏi đáp',
    );
    expect(fixture.nativeElement.querySelector('.topic-tag')).toBeNull();
  });

  it('renders an unclassified post as a general community post', () => {
    const page = postPage();
    page.items = [{ ...postView(), postTypeId: null }];
    listPublicPosts.mockReturnValue(of({ result: page }));
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();

    expect(fixture.componentInstance.posts()[0]).toEqual(
      expect.objectContaining({ category: null, categoryLabel: undefined }),
    );
    expect(fixture.nativeElement.querySelector('.category-tag')).toBeNull();
  });

  it('renders post-shaped skeletons while the initial feed request is pending', () => {
    const feedRequest = new Subject<{ result: ReturnType<typeof postPage> }>();
    listPublicPosts.mockReturnValue(feedRequest);
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();

    const skeleton = fixture.nativeElement.querySelector('.feed-skeleton');
    expect(skeleton).not.toBeNull();
    expect(skeleton.getAttribute('aria-busy')).toBe('true');
    expect(skeleton.querySelectorAll('.post-skeleton')).toHaveLength(3);
    expect(fixture.nativeElement.querySelector('app-post-feed')).toBeNull();

    feedRequest.next({ result: postPage() });
    feedRequest.complete();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.feed-skeleton')).toBeNull();
    expect(fixture.nativeElement.querySelector('app-post-feed')).not.toBeNull();
  });

  it('loads the root comment total before the comment panel is opened', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();

    expect(listPostComments).toHaveBeenCalledWith('post-1', 0, 1);
    expect(fixture.componentInstance.posts()[0].commentRootTotal).toBe(1);
    expect(fixture.componentInstance.posts()[0].commentsLoaded).toBe(false);
  });

  it('loads bookmark and share state and applies both interactions', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    const postCard = fixture.debugElement.query(By.directive(PostCard))
      .componentInstance as PostCard;

    expect(component.posts()[0].saved).toBe(false);
    expect(component.posts()[0].shares).toBe(3);

    postCard.bookmarkToggled.emit();
    expect(savePostBookmark).toHaveBeenCalledWith('post-1');
    expect(component.posts()[0].saved).toBe(true);
    expect(component.posts()[0].bookmarkPending).toBe(false);

    postCard.bookmarkToggled.emit();
    expect(removePostBookmark).toHaveBeenCalledWith('post-1');
    expect(component.posts()[0].saved).toBe(false);

    postCard.shareRequested.emit();
    expect(createPostShare).toHaveBeenCalledWith('post-1');
    expect(component.posts()[0].shares).toBe(4);
    expect(component.posts()[0].sharePending).toBe(false);
  });

  it('loads the paginated bookmarked feed and marks every returned post as saved', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component.selectFilter('SAVED');

    expect(listMyBookmarkedPosts).toHaveBeenCalledWith({ page: 0, size: 20 });
    expect(component.posts()[0].saved).toBe(true);
    expect(getPostBookmarkStatus).toHaveBeenCalledOnce();
  });

  it('optimistically switches reactions and restores the exact snapshot on API failure', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    const postCard = fixture.debugElement.query(By.directive(PostCard))
      .componentInstance as PostCard;

    postCard.reactionChanged.emit('LOVE');
    let post = component.posts()[0];
    expect(post.currentReaction).toBe('LOVE');
    expect(post.reactionCounts.LIKE).toBe(1);
    expect(post.reactionCounts.LOVE).toBe(1);
    expect(post.reactionPending).toBe(true);

    reactionMutation.error(
      new HttpErrorResponse({
        status: 503,
        error: { code: 'SYS_INTERNAL_ERROR', message: 'Unavailable' },
      }),
    );

    post = component.posts()[0];
    expect(post.currentReaction).toBe('LIKE');
    expect(post.reactionCounts.LIKE).toBe(2);
    expect(post.reactionCounts.LOVE).toBe(0);
    expect(post.reactionTotal).toBe(2);
    expect(post.reactionPending).toBe(false);
    expect(post.reactionError).not.toBe('');
  });

  it('loads root comments with replies and applies create, edit, and delete results', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    const postCard = fixture.debugElement.query(By.directive(PostCard))
      .componentInstance as PostCard;

    postCard.commentsRequested.emit(false);
    let post = component.posts()[0];
    expect(post.commentsLoaded).toBe(true);
    expect(post.commentRootTotal).toBe(1);
    expect(post.comments[0].replies.map((reply) => reply.id)).toEqual(['reply-1']);

    postCard.commentCreated.emit({ parentCommentId: 'comment-1', content: 'Phản hồi mới' });
    post = component.posts()[0];
    expect(createPostComment).toHaveBeenCalledWith('post-1', {
      content: 'Phản hồi mới',
      parentCommentId: 'comment-1',
    });
    expect(post.comments[0].replies.map((reply) => reply.id)).toEqual(['reply-1', 'reply-new']);
    expect(post.commentCreateVersion).toBe(1);

    postCard.commentUpdated.emit({ commentId: 'comment-1', content: 'Nội dung đã sửa' });
    post = component.posts()[0];
    expect(updatePostComment).toHaveBeenCalledWith('post-1', 'comment-1', {
      content: 'Nội dung đã sửa',
    });
    expect(post.comments[0].content).toBe('Nội dung đã sửa');
    expect(post.comments[0].edited).toBe(true);

    postCard.commentDeleted.emit('reply-new');
    post = component.posts()[0];
    expect(deletePostComment).toHaveBeenCalledWith('post-1', 'reply-new');
    expect(post.comments[0].replies.map((reply) => reply.id)).toEqual(['reply-1']);
  });

  it('sends a report for another author and marks the post as reported', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.posts.update((posts) =>
      posts.map((post) => ({ ...post, author: { ...post.author, id: 'author-2' } })),
    );

    component.reportPost({
      postId: 'post-1',
      reason: 'MISINFORMATION',
      reasonDetail: 'Thông tin chưa chính xác',
    });

    expect(createPostReport).toHaveBeenCalledWith('post-1', {
      reason: 'MISINFORMATION',
      reasonDetail: 'Thông tin chưa chính xác',
    });
    expect(component.posts()[0].reported).toBe(true);
    expect(component.posts()[0].reportPending).toBe(false);
  });

  it('deletes an owned post from the mine feed after the API succeeds', () => {
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.selectFilter('MINE');
    fixture.detectChanges();
    const postCard = fixture.debugElement.query(By.directive(PostCard))
      .componentInstance as PostCard;

    postCard.deleteRequested.emit();

    expect(deletePost).toHaveBeenCalledWith('post-1');
    expect(component.posts()).toEqual([]);
    expect(component.announcement()).toBe('Đã xóa bài viết.');
  });

  it('keeps an owned post visible and shows a friendly error when deletion fails', () => {
    deletePost.mockReturnValue(throwError(() => new Error('Post service unavailable')));
    const fixture = TestBed.createComponent(Community);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    const postCard = fixture.debugElement.query(By.directive(PostCard))
      .componentInstance as PostCard;

    postCard.deleteRequested.emit();

    expect(component.posts()).toHaveLength(1);
    expect(component.posts()[0].deletePending).toBe(false);
    expect(component.posts()[0].deleteError).toBe('Không thể xóa bài viết. Vui lòng thử lại.');
  });

  function reactionSummary(): PostReactionSummaryView {
    return {
      totalCount: 2,
      counts: { LIKE: 2, LOVE: 0, HAHA: 0, WOW: 0, SAD: 0, ANGRY: 0 },
      currentUserReaction: 'LIKE',
    };
  }

  function postPage() {
    return {
      items: [postView()],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
      hasNext: false,
    };
  }

  function postView(): PostView {
    return {
      id: 'post-1',
      authorUserId: 'user-1',
      postTypeId: 'type-1',
      topicId: null,
      content: 'Kinh nghiệm chăm lúa',
      locationText: 'Đồng Tháp',
      visibility: 'PUBLIC',
      status: 'PUBLISHED',
      media: [],
      cropTypeIds: ['crop-1'],
      publishedAt: '2026-08-24T00:00:00Z',
      createdAt: '2026-08-24T00:00:00Z',
      updatedAt: '2026-08-24T00:00:00Z',
    };
  }

  function postType() {
    return {
      id: 'type-1',
      code: 'EXPERIENCE',
      name: 'Kinh nghiệm',
      description: null,
      displayOrder: 0,
      active: true,
      createdAt: '2026-08-24T00:00:00Z',
      updatedAt: '2026-08-24T00:00:00Z',
    };
  }

  function cropType(): CropTypeView {
    return {
      id: 'crop-1',
      code: 'RICE',
      name: 'Lúa',
      description: null,
      active: true,
    };
  }

  function fileView(id: string, originalFileName: string) {
    return {
      id,
      ownerUserId: 'user-1',
      purpose: 'POST_IMAGE',
      originalFileName,
      contentType: 'image/jpeg',
      sizeBytes: 3,
      publicUrl: `https://files.example/${id}`,
      status: 'UPLOADED',
      createdAt: '2026-08-24T00:00:00Z',
      updatedAt: '2026-08-24T00:00:00Z',
    };
  }

  function commentPage() {
    const root = commentView('comment-1', null);
    const reply = commentView('reply-1', root.id);
    const item: PostCommentThreadView = { comment: root, replies: [reply] };
    return {
      items: [item],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
      hasNext: false,
    };
  }

  function commentView(id: string, parentCommentId: string | null): PostCommentView {
    return {
      id,
      postId: 'post-1',
      parentCommentId,
      authorUserId: 'user-1',
      content: id === 'comment-1' ? 'Bình luận gốc' : 'Phản hồi',
      status: 'PUBLISHED',
      createdAt: '2026-08-24T01:00:00Z',
      updatedAt: '2026-08-24T01:00:00Z',
    };
  }
});
