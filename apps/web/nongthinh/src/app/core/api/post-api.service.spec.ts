import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PostApiService } from './post-api.service';

describe('PostApiService', () => {
  let service: PostApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PostApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads the requested page of users who reacted to a post', () => {
    service.listPostReactions('post-1', 2, 10, 'LOVE').subscribe();
    const request = http.expectOne(
      '/api/v1/posts/post-1/reactions/users?page=2&size=10&reactionType=LOVE',
    );
    expect(request.request.method).toBe('GET');
    request.flush({ result: { items: [], page: 2, size: 10, totalElements: 0, totalPages: 0, hasNext: false } });
  });

  it('loads the complete post type catalog for administrators', () => {
    service.listPostTypes().subscribe();
    const request = http.expectOne('/api/v1/posts/admin/post-types');
    expect(request.request.method).toBe('GET');
    request.flush({ result: [] });
  });

  it('creates a post topic with the expected contract', () => {
    const payload = { name: 'Sâu bệnh', slug: 'sau-benh', description: null, displayOrder: 10 };
    service.createPostTopic(payload).subscribe();
    const request = http.expectOne('/api/v1/posts/admin/post-topics');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ result: null });
  });

  it('loads ranked trending topics with the requested limit', () => {
    service.listTrendingPostTopics(5).subscribe();

    const request = http.expectOne(
      (candidate) =>
        candidate.url === '/api/v1/posts/post-topics/trending' &&
        candidate.params.get('limit') === '5',
    );
    expect(request.request.method).toBe('GET');
    request.flush({ result: [] });
  });

  it('loads the public feed with backend pagination and filters', () => {
    service
      .listPublicPosts({
        postTypeId: 'type-1', authorUserId: 'author-1', keyword: '  cây lúa  ', page: 2, size: 10,
      })
      .subscribe();

    const request = http.expectOne(
      (candidate) =>
        candidate.url === '/api/v1/posts/' &&
          candidate.params.get('postTypeId') === 'type-1' &&
          candidate.params.get('authorUserId') === 'author-1' &&
        candidate.params.get('keyword') === 'cây lúa' &&
        candidate.params.get('page') === '2' &&
        candidate.params.get('size') === '10',
    );
    expect(request.request.method).toBe('GET');
    expect(request.request.params.has('topicId')).toBe(false);
    request.flush({
      result: { items: [], page: 2, size: 10, totalElements: 0, totalPages: 0, hasNext: false },
    });
  });

  it('creates a draft using the post-service request contract', () => {
    const payload = {
      postTypeId: 'type-1',
      topicId: null,
      content: 'Kinh nghiệm chăm lúa',
      locationText: 'Đồng Tháp',
      visibility: 'PUBLIC' as const,
      status: 'DRAFT' as const,
      cropTypeIds: ['crop-1', 'crop-2'],
    };

    service.createPost(payload).subscribe();
    const request = http.expectOne('/api/v1/posts/');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ result: { id: 'post-1' } });
  });

  it('attaches uploaded media and publishes the draft', () => {
    service
      .createPostMedia('post-1', { fileId: 'file-1', displayOrder: 0, caption: null })
      .subscribe();
    const mediaRequest = http.expectOne('/api/v1/posts/post-1/media');
    expect(mediaRequest.request.method).toBe('POST');
    expect(mediaRequest.request.body).toEqual({ fileId: 'file-1', displayOrder: 0, caption: null });
    mediaRequest.flush({ result: { id: 'media-1' } });

    service.publishPost('post-1').subscribe();
    const publishRequest = http.expectOne('/api/v1/posts/post-1/publish');
    expect(publishRequest.request.method).toBe('POST');
    expect(publishRequest.request.body).toBeNull();
    publishRequest.flush({ result: { id: 'post-1', status: 'PUBLISHED' } });
  });

  it('uses the comment thread endpoints and request contracts', () => {
    service.listPostComments('post-1', 1, 5).subscribe();
    const listRequest = http.expectOne(
      (candidate) =>
        candidate.url === '/api/v1/posts/post-1/comments' &&
        candidate.params.get('page') === '1' &&
        candidate.params.get('size') === '5',
    );
    expect(listRequest.request.method).toBe('GET');
    listRequest.flush({
      result: { items: [], page: 1, size: 5, totalElements: 0, totalPages: 0, hasNext: false },
    });

    service
      .createPostComment('post-1', { content: 'Cảm ơn chia sẻ', parentCommentId: 'comment-1' })
      .subscribe();
    const createRequest = http.expectOne('/api/v1/posts/post-1/comments');
    expect(createRequest.request.method).toBe('POST');
    expect(createRequest.request.body).toEqual({
      content: 'Cảm ơn chia sẻ',
      parentCommentId: 'comment-1',
    });
    createRequest.flush({ result: { id: 'reply-1' } });

    service.updatePostComment('post-1', 'comment-1', { content: 'Nội dung đã sửa' }).subscribe();
    const updateRequest = http.expectOne('/api/v1/posts/post-1/comments/comment-1');
    expect(updateRequest.request.method).toBe('PUT');
    expect(updateRequest.request.body).toEqual({ content: 'Nội dung đã sửa' });
    updateRequest.flush({ result: { id: 'comment-1', content: 'Nội dung đã sửa' } });

    service.deletePostComment('post-1', 'comment-1').subscribe();
    const deleteRequest = http.expectOne('/api/v1/posts/post-1/comments/comment-1');
    expect(deleteRequest.request.method).toBe('DELETE');
    deleteRequest.flush({ result: null });
  });

  it('loads, sets, and removes the current reaction', () => {
    service.getPostReactionSummary('post-1').subscribe();
    const summaryRequest = http.expectOne('/api/v1/posts/post-1/reactions');
    expect(summaryRequest.request.method).toBe('GET');
    summaryRequest.flush({
      result: {
        totalCount: 1,
        counts: { LIKE: 1, LOVE: 0, HAHA: 0, WOW: 0, SAD: 0, ANGRY: 0 },
        currentUserReaction: 'LIKE',
      },
    });

    service.setPostReaction('post-1', { reactionType: 'LOVE' }).subscribe();
    const setRequest = http.expectOne('/api/v1/posts/post-1/reactions');
    expect(setRequest.request.method).toBe('PUT');
    expect(setRequest.request.body).toEqual({ reactionType: 'LOVE' });
    setRequest.flush({ result: { id: 'reaction-1', reactionType: 'LOVE' } });

    service.removePostReaction('post-1').subscribe();
    const removeRequest = http.expectOne('/api/v1/posts/post-1/reactions');
    expect(removeRequest.request.method).toBe('DELETE');
    removeRequest.flush({ result: null });
  });

  it('lists, reads, saves, and removes post bookmarks', () => {
    service.listMyBookmarkedPosts({ page: 1, size: 10 }).subscribe();
    const listRequest = http.expectOne(
      (candidate) =>
        candidate.url === '/api/v1/posts/bookmarks' &&
        candidate.params.get('page') === '1' &&
        candidate.params.get('size') === '10',
    );
    expect(listRequest.request.method).toBe('GET');
    listRequest.flush({
      result: { items: [], page: 1, size: 10, totalElements: 0, totalPages: 0, hasNext: false },
    });

    service.getPostBookmarkStatus('post-1').subscribe();
    const statusRequest = http.expectOne('/api/v1/posts/post-1/bookmarks');
    expect(statusRequest.request.method).toBe('GET');
    statusRequest.flush({ result: { postId: 'post-1', bookmarked: false } });

    service.savePostBookmark('post-1').subscribe();
    const saveRequest = http.expectOne('/api/v1/posts/post-1/bookmarks');
    expect(saveRequest.request.method).toBe('PUT');
    expect(saveRequest.request.body).toBeNull();
    saveRequest.flush({ result: { postId: 'post-1', userId: 'user-1' } });

    service.removePostBookmark('post-1').subscribe();
    const removeRequest = http.expectOne('/api/v1/posts/post-1/bookmarks');
    expect(removeRequest.request.method).toBe('DELETE');
    removeRequest.flush({ result: null });
  });

  it('loads the share summary and records a share', () => {
    service.getPostShareSummary('post-1').subscribe();
    const summaryRequest = http.expectOne('/api/v1/posts/post-1/shares');
    expect(summaryRequest.request.method).toBe('GET');
    summaryRequest.flush({ result: { postId: 'post-1', totalCount: 3 } });

    service.createPostShare('post-1').subscribe();
    const createRequest = http.expectOne('/api/v1/posts/post-1/shares');
    expect(createRequest.request.method).toBe('POST');
    expect(createRequest.request.body).toBeNull();
    createRequest.flush({ result: { id: 'share-1', postId: 'post-1' } });
  });

  it('uses the user and administrator post-report contracts', () => {
    const reportPayload = { reason: 'MISINFORMATION' as const, reasonDetail: 'Thông tin sai' };
    service.createPostReport('post-1', reportPayload).subscribe();
    const createRequest = http.expectOne('/api/v1/posts/post-1/reports');
    expect(createRequest.request.method).toBe('POST');
    expect(createRequest.request.body).toEqual(reportPayload);
    createRequest.flush({ result: { id: 'report-1' } });

    service.listPostReports({ status: 'PENDING', page: 1, size: 20 }).subscribe();
    const listRequest = http.expectOne(
      (candidate) =>
        candidate.url === '/api/v1/posts/admin/post-reports' &&
        candidate.params.get('status') === 'PENDING' &&
        candidate.params.get('page') === '1' &&
        candidate.params.get('size') === '20',
    );
    expect(listRequest.request.method).toBe('GET');
    listRequest.flush({
      result: { items: [], page: 1, size: 20, totalElements: 0, totalPages: 0, hasNext: false },
    });

    service.getPostReport('report-1').subscribe();
    const detailRequest = http.expectOne('/api/v1/posts/admin/post-reports/report-1');
    expect(detailRequest.request.method).toBe('GET');
    detailRequest.flush({ result: { id: 'report-1' } });

    service.startPostReportReview('report-1').subscribe();
    const reviewRequest = http.expectOne('/api/v1/posts/admin/post-reports/report-1/review');
    expect(reviewRequest.request.method).toBe('POST');
    expect(reviewRequest.request.body).toBeNull();
    reviewRequest.flush({ result: { id: 'report-1', status: 'UNDER_REVIEW' } });

    service.resolvePostReport('report-1', { resolutionNote: 'Đã xử lý' }).subscribe();
    const resolveRequest = http.expectOne('/api/v1/posts/admin/post-reports/report-1/resolve');
    expect(resolveRequest.request.method).toBe('POST');
    expect(resolveRequest.request.body).toEqual({ resolutionNote: 'Đã xử lý' });
    resolveRequest.flush({ result: { id: 'report-1', status: 'RESOLVED' } });

    service.rejectPostReport('report-2', { resolutionNote: null }).subscribe();
    const rejectRequest = http.expectOne('/api/v1/posts/admin/post-reports/report-2/reject');
    expect(rejectRequest.request.method).toBe('POST');
    expect(rejectRequest.request.body).toEqual({ resolutionNote: null });
    rejectRequest.flush({ result: { id: 'report-2', status: 'REJECTED' } });
  });

  it('uses only the administrator post-history contracts', () => {
    service
      .listAdminPostHistories({
        postId: 'post-1',
        postAuthorUserId: 'author-1',
        actorUserId: 'actor-1',
        actorType: 'ADMIN',
        action: 'HIDDEN',
        page: 0,
        size: 20,
      })
      .subscribe();
    const adminListRequest = http.expectOne(
      (candidate) =>
        candidate.url === '/api/v1/posts/admin/post-histories' &&
        candidate.params.get('postId') === 'post-1' &&
        candidate.params.get('postAuthorUserId') === 'author-1' &&
        candidate.params.get('actorUserId') === 'actor-1' &&
        candidate.params.get('actorType') === 'ADMIN' &&
        candidate.params.get('action') === 'HIDDEN',
    );
    expect(adminListRequest.request.method).toBe('GET');
    adminListRequest.flush({
      result: { items: [], page: 0, size: 20, totalElements: 0, totalPages: 0, hasNext: false },
    });

    service.getAdminPostHistory('history-1').subscribe();
    const adminDetailRequest = http.expectOne('/api/v1/posts/admin/post-histories/history-1');
    expect(adminDetailRequest.request.method).toBe('GET');
    adminDetailRequest.flush({ result: { id: 'history-1' } });
  });

  it('uses administrator post search, detail, and statistics contracts', () => {
    service
      .listAdminPosts({
        authorUserId: 'author-1',
        status: 'PUBLISHED',
        keyword: '  cây lúa  ',
        page: 1,
        size: 12,
      })
      .subscribe();
    const listRequest = http.expectOne(
      (candidate) =>
        candidate.url === '/api/v1/posts/admin/posts' &&
        candidate.params.get('authorUserId') === 'author-1' &&
        candidate.params.get('status') === 'PUBLISHED' &&
        candidate.params.get('keyword') === 'cây lúa' &&
        candidate.params.get('page') === '1' &&
        candidate.params.get('size') === '12',
    );
    expect(listRequest.request.method).toBe('GET');
    listRequest.flush({
      result: { items: [], page: 1, size: 12, totalElements: 0, totalPages: 0, hasNext: false },
    });

    service.getAdminPost('post-1').subscribe();
    const detailRequest = http.expectOne('/api/v1/posts/admin/posts/post-1');
    expect(detailRequest.request.method).toBe('GET');
    detailRequest.flush({ result: { id: 'post-1' } });

    service
      .getPostStatistics({
        from: '2026-08-21T17:00:00.000Z',
        to: '2026-08-28T17:00:00.000Z',
        bucket: 'DAY',
      })
      .subscribe();
    const statisticsRequest = http.expectOne(
      (candidate) =>
        candidate.url === '/api/v1/posts/admin/posts/statistics' &&
        candidate.params.get('bucket') === 'DAY' &&
        candidate.params.get('timeZone') === 'Asia/Ho_Chi_Minh',
    );
    expect(statisticsRequest.request.method).toBe('GET');
    statisticsRequest.flush({ result: { totalPosts: 0, postsInRange: 0, timeline: [] } });
  });
});
