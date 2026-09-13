import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response';
import { CommentMetrics, PostEngagement, ReactionMetrics } from '../realtime/realtime.models';

export interface PostTypeView {
  id: string;
  code: string;
  name: string;
  description: string | null;
  displayOrder: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PostTopicView {
  id: string;
  name: string;
  slug: string;
  description: string | null;
  displayOrder: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TrendingPostTopicView {
  rank: number;
  id: string;
  name: string;
  slug: string;
  postCount: number;
}

export type PostStatus = 'DRAFT' | 'PUBLISHED' | 'HIDDEN';
export type PostVisibility = 'PUBLIC' | 'PRIVATE' | 'FOLLOWERS';
export type PostMediaType = 'IMAGE' | 'VIDEO';
export const POST_REACTION_TYPES = ['LIKE', 'LOVE', 'HAHA', 'WOW', 'SAD', 'ANGRY'] as const;
export type PostReactionType = (typeof POST_REACTION_TYPES)[number];
export type PostCommentStatus = 'PUBLISHED' | 'HIDDEN';
export const POST_REPORT_REASONS = [
  'SPAM',
  'HARASSMENT',
  'HATE_SPEECH',
  'VIOLENCE',
  'SEXUAL_CONTENT',
  'MISINFORMATION',
  'COPYRIGHT',
  'OTHER',
] as const;
export type PostReportReason = (typeof POST_REPORT_REASONS)[number];
export const POST_REPORT_STATUSES = ['PENDING', 'UNDER_REVIEW', 'RESOLVED', 'REJECTED'] as const;
export type PostReportStatus = (typeof POST_REPORT_STATUSES)[number];
export const POST_HISTORY_ACTIONS = [
  'CREATED',
  'UPDATED',
  'PUBLISHED',
  'VISIBILITY_CHANGED',
  'HIDDEN',
  'RESTORED',
  'DELETED',
] as const;
export type PostHistoryAction = (typeof POST_HISTORY_ACTIONS)[number];
export type PostHistoryActorType = 'USER' | 'ADMIN' | 'SYSTEM';

export interface PostMediaView {
  id: string;
  fileId: string;
  mediaType: PostMediaType;
  mediaUrl: string;
  contentType: string;
  width: number | null;
  height: number | null;
  sizeBytes: number | null;
  displayOrder: number;
  caption: string | null;
  createdAt: string;
}

export interface PostView {
  id: string;
  authorUserId: string;
  postTypeId: string | null;
  topicId: string | null;
  content: string;
  locationText: string | null;
  visibility: PostVisibility;
  status: PostStatus;
  media: PostMediaView[];
  cropTypeIds: string[];
  publishedAt: string | null;
  createdAt: string;
  updatedAt: string;
  deletedAt?: string | null;
}

export interface PostCommentView extends Partial<CommentMetrics> {
  id: string;
  postId: string;
  parentCommentId: string | null;
  authorUserId: string;
  content: string;
  status: PostCommentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface PostCommentThreadView {
  comment: PostCommentView;
  replies: PostCommentView[];
}

export interface PostReactionSummaryView {
  reactionVersion?: number;
  totalCount: number;
  counts: Record<PostReactionType, number>;
  currentUserReaction: PostReactionType | null;
}

export interface PostReactionView extends Partial<ReactionMetrics> {
  id: string;
  postId: string;
  actorId: string;
  reactionType: PostReactionType;
  createdAt: string;
  updatedAt: string;
}

export interface PostBookmarkStatusView {
  postId: string;
  bookmarked: boolean;
}

export interface PostBookmarkView {
  postId: string;
  userId: string;
  createdAt: string;
}

export interface PostShareSummaryView {
  postId: string;
  totalCount: number;
}

export interface PostShareView {
  id: string;
  postId: string;
  sharedByUserId: string;
  createdAt: string;
}

export interface PostReportView {
  id: string;
  postId: string;
  reporterId: string;
  reason: PostReportReason;
  reasonDetail: string | null;
  status: PostReportStatus;
  resolvedBy: string | null;
  resolvedAt: string | null;
  resolutionNote: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PostHistoryMediaSnapshotView {
  fileId: string;
  mediaUrl: string;
  contentType: string;
  displayOrder: number;
}

export interface PostHistorySnapshotView {
  content: string;
  postTypeId: string | null;
  postTopicId: string | null;
  cropTypeIds: string[];
  locationText: string | null;
  media: PostHistoryMediaSnapshotView[];
  status: PostStatus;
  visibility: PostVisibility;
}

export interface PostHistoryView {
  id: string;
  postId: string;
  postAuthorUserId: string;
  actorUserId: string | null;
  actorType: PostHistoryActorType;
  action: PostHistoryAction;
  previousStatus: PostStatus | null;
  newStatus: PostStatus | null;
  previousVisibility: PostVisibility | null;
  newVisibility: PostVisibility | null;
  reasonCode: string | null;
  reasonDetail: string | null;
  reportId: string | null;
  snapshot: PostHistorySnapshotView;
  createdAt: string;
}

export interface PageView<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export type PostStatisticsBucket = 'HOUR' | 'DAY';

export interface PostTimelinePointView {
  bucketStart: string;
  bucketEnd: string;
  count: number;
}

export interface PostStatisticsView {
  totalPosts: number;
  postsInRange: number;
  from: string;
  to: string;
  bucket: PostStatisticsBucket;
  timeZone: string;
  statusCounts: Record<PostStatus, number>;
  timeline: PostTimelinePointView[];
}

export interface ListPublicPostsParams {
  authorUserId?: string | null;
  postTypeId?: string | null;
  topicId?: string | null;
  cropTypeId?: string | null;
  keyword?: string | null;
  page?: number;
  size?: number;
}

export interface ListMyPostsParams {
  status?: PostStatus | null;
  page?: number;
  size?: number;
}

export interface ListBookmarkedPostsParams {
  page?: number;
  size?: number;
}

export interface CreatePostPayload {
  postTypeId: string | null;
  topicId: string | null;
  content: string;
  locationText: string | null;
  visibility: PostVisibility;
  status: 'DRAFT' | 'PUBLISHED';
  cropTypeIds: string[];
}

export interface CreatePostMediaPayload {
  fileId: string;
  displayOrder: number;
  caption: string | null;
}

export interface CreatePostCommentPayload {
  content: string;
  parentCommentId: string | null;
}

export interface UpdatePostCommentPayload {
  content: string;
}

export interface SetPostReactionPayload {
  reactionType: PostReactionType;
}

export interface CreatePostReportPayload {
  reason: PostReportReason;
  reasonDetail: string | null;
}

export interface CompletePostReportPayload {
  resolutionNote: string | null;
  moderationAction?: 'HIDE' | 'DELETE' | null;
}

export interface ListPostReportsParams {
  status?: PostReportStatus | null;
  page?: number;
  size?: number;
}

export interface ListAdminPostHistoriesParams {
  postId?: string | null;
  action?: PostHistoryAction | null;
  page?: number;
  size?: number;
  postAuthorUserId?: string | null;
  actorUserId?: string | null;
  actorType?: PostHistoryActorType | null;
}

export interface ListAdminPostsParams {
  authorUserId?: string | null;
  status?: PostStatus | null;
  keyword?: string | null;
  from?: string | null;
  to?: string | null;
  page?: number;
  size?: number;
}

export interface GetPostStatisticsParams {
  from: string;
  to: string;
  bucket: PostStatisticsBucket;
  timeZone?: string;
}

export interface CreatePostTypePayload {
  code: string;
  name: string;
  description: string | null;
  displayOrder: number;
}

export interface UpdatePostTypePayload {
  name: string;
  description: string | null;
  displayOrder: number;
  active: boolean;
}

export interface CreatePostTopicPayload {
  name: string;
  slug: string;
  description: string | null;
  displayOrder: number;
}

export type UpdatePostTopicPayload = UpdatePostTypePayload;

@Injectable({ providedIn: 'root' })
export class PostApiService {
  getPost(postId: string): Observable<ApiResponse<PostView>> {
    return this.http.get<ApiResponse<PostView>>(`${this.baseUrl}/${postId}`);
  }
  getPostEngagement(postId: string): Observable<ApiResponse<PostEngagement>> {
    return this.http.get<ApiResponse<PostEngagement>>(`${this.baseUrl}/${postId}/engagement`);
  }
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/posts';

  listActivePostTypes(): Observable<ApiResponse<PostTypeView[]>> {
    return this.http.get<ApiResponse<PostTypeView[]>>(`${this.baseUrl}/post-types`);
  }

  listActivePostTopics(): Observable<ApiResponse<PostTopicView[]>> {
    return this.http.get<ApiResponse<PostTopicView[]>>(`${this.baseUrl}/post-topics`);
  }

  listTrendingPostTopics(limit = 3): Observable<ApiResponse<TrendingPostTopicView[]>> {
    const params = new HttpParams().set('limit', String(limit));
    return this.http.get<ApiResponse<TrendingPostTopicView[]>>(
      `${this.baseUrl}/post-topics/trending`,
      { params },
    );
  }

  listPublicPosts(params: ListPublicPostsParams = {}): Observable<ApiResponse<PageView<PostView>>> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 20));

    const optionalParams: Record<string, string | null | undefined> = {
      postTypeId: params.postTypeId,
      topicId: params.topicId,
      cropTypeId: params.cropTypeId,
      keyword: params.keyword?.trim() || null,
      authorUserId: params.authorUserId,
    };
    for (const [key, value] of Object.entries(optionalParams)) {
      if (value) httpParams = httpParams.set(key, value);
    }

    // The post-service is deployed with context path /posts. Calling the context
    // root without a trailing slash makes Tomcat issue an absolute redirect to
    // localhost:9100, bypassing the API Gateway and breaking browser CORS.
    return this.http.get<ApiResponse<PageView<PostView>>>(`${this.baseUrl}/`, {
      params: httpParams,
    });
  }

  listMyPosts(params: ListMyPostsParams = {}): Observable<ApiResponse<PageView<PostView>>> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 20));
    if (params.status) httpParams = httpParams.set('status', params.status);
    return this.http.get<ApiResponse<PageView<PostView>>>(`${this.baseUrl}/me`, {
      params: httpParams,
    });
  }

  createPost(payload: CreatePostPayload): Observable<ApiResponse<PostView>> {
    return this.http.post<ApiResponse<PostView>>(`${this.baseUrl}/`, payload);
  }

  publishPost(id: string): Observable<ApiResponse<PostView>> {
    return this.http.post<ApiResponse<PostView>>(`${this.baseUrl}/${id}/publish`, null);
  }

  deletePost(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }

  createPostMedia(
    postId: string,
    payload: CreatePostMediaPayload,
  ): Observable<ApiResponse<PostMediaView>> {
    return this.http.post<ApiResponse<PostMediaView>>(`${this.baseUrl}/${postId}/media`, payload);
  }

  listPostComments(
    postId: string,
    page = 0,
    size = 20,
  ): Observable<ApiResponse<PageView<PostCommentThreadView>>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<ApiResponse<PageView<PostCommentThreadView>>>(
      `${this.baseUrl}/${postId}/comments`,
      { params },
    );
  }

  createPostComment(
    postId: string,
    payload: CreatePostCommentPayload,
  ): Observable<ApiResponse<PostCommentView>> {
    return this.http.post<ApiResponse<PostCommentView>>(
      `${this.baseUrl}/${postId}/comments`,
      payload,
    );
  }

  updatePostComment(
    postId: string,
    commentId: string,
    payload: UpdatePostCommentPayload,
  ): Observable<ApiResponse<PostCommentView>> {
    return this.http.put<ApiResponse<PostCommentView>>(
      `${this.baseUrl}/${postId}/comments/${commentId}`,
      payload,
    );
  }

  deletePostComment(postId: string, commentId: string): Observable<ApiResponse<CommentMetrics & { deletedCommentId: string }>> {
    return this.http.delete<ApiResponse<CommentMetrics & { deletedCommentId: string }>>(`${this.baseUrl}/${postId}/comments/${commentId}`);
  }

  getPostReactionSummary(postId: string): Observable<ApiResponse<PostReactionSummaryView>> {
    return this.http.get<ApiResponse<PostReactionSummaryView>>(
      `${this.baseUrl}/${postId}/reactions`,
    );
  }

  listPostReactions(
    postId: string,
    page = 0,
    size = 20,
    reactionType: PostReactionType | null = null,
  ): Observable<ApiResponse<PageView<PostReactionView>>> {
    let params = new HttpParams().set('page', String(page)).set('size', String(size));
    if (reactionType) params = params.set('reactionType', reactionType);
    return this.http.get<ApiResponse<PageView<PostReactionView>>>(
      `${this.baseUrl}/${postId}/reactions/users`, { params },
    );
  }

  setPostReaction(
    postId: string,
    payload: SetPostReactionPayload,
  ): Observable<ApiResponse<PostReactionView>> {
    return this.http.put<ApiResponse<PostReactionView>>(
      `${this.baseUrl}/${postId}/reactions`,
      payload,
    );
  }

  removePostReaction(postId: string): Observable<ApiResponse<PostEngagement>> {
    return this.http.delete<ApiResponse<PostEngagement>>(`${this.baseUrl}/${postId}/reactions`);
  }

  listMyBookmarkedPosts(
    params: ListBookmarkedPostsParams = {},
  ): Observable<ApiResponse<PageView<PostView>>> {
    const httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 20));
    return this.http.get<ApiResponse<PageView<PostView>>>(`${this.baseUrl}/bookmarks`, {
      params: httpParams,
    });
  }

  getPostBookmarkStatus(postId: string): Observable<ApiResponse<PostBookmarkStatusView>> {
    return this.http.get<ApiResponse<PostBookmarkStatusView>>(
      `${this.baseUrl}/${postId}/bookmarks`,
    );
  }

  savePostBookmark(postId: string): Observable<ApiResponse<PostBookmarkView>> {
    return this.http.put<ApiResponse<PostBookmarkView>>(
      `${this.baseUrl}/${postId}/bookmarks`,
      null,
    );
  }

  removePostBookmark(postId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${postId}/bookmarks`);
  }

  getPostShareSummary(postId: string): Observable<ApiResponse<PostShareSummaryView>> {
    return this.http.get<ApiResponse<PostShareSummaryView>>(`${this.baseUrl}/${postId}/shares`);
  }

  createPostShare(postId: string): Observable<ApiResponse<PostShareView>> {
    return this.http.post<ApiResponse<PostShareView>>(`${this.baseUrl}/${postId}/shares`, null);
  }

  createPostReport(
    postId: string,
    payload: CreatePostReportPayload,
  ): Observable<ApiResponse<PostReportView>> {
    return this.http.post<ApiResponse<PostReportView>>(
      `${this.baseUrl}/${postId}/reports`,
      payload,
    );
  }

  listPostReports(
    params: ListPostReportsParams = {},
  ): Observable<ApiResponse<PageView<PostReportView>>> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 20));
    if (params.status) httpParams = httpParams.set('status', params.status);
    return this.http.get<ApiResponse<PageView<PostReportView>>>(
      `${this.baseUrl}/admin/post-reports`,
      { params: httpParams },
    );
  }

  getPostReport(reportId: string): Observable<ApiResponse<PostReportView>> {
    return this.http.get<ApiResponse<PostReportView>>(
      `${this.baseUrl}/admin/post-reports/${reportId}`,
    );
  }

  startPostReportReview(reportId: string): Observable<ApiResponse<PostReportView>> {
    return this.http.post<ApiResponse<PostReportView>>(
      `${this.baseUrl}/admin/post-reports/${reportId}/review`,
      null,
    );
  }

  resolvePostReport(
    reportId: string,
    payload: CompletePostReportPayload,
  ): Observable<ApiResponse<PostReportView>> {
    return this.http.post<ApiResponse<PostReportView>>(
      `${this.baseUrl}/admin/post-reports/${reportId}/resolve`,
      payload,
    );
  }

  rejectPostReport(
    reportId: string,
    payload: CompletePostReportPayload,
  ): Observable<ApiResponse<PostReportView>> {
    return this.http.post<ApiResponse<PostReportView>>(
      `${this.baseUrl}/admin/post-reports/${reportId}/reject`,
      payload,
    );
  }

  listAdminPosts(params: ListAdminPostsParams = {}): Observable<ApiResponse<PageView<PostView>>> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 20));
    const optionalParams: Record<string, string | null | undefined> = {
      authorUserId: params.authorUserId,
      status: params.status,
      keyword: params.keyword?.trim() || null,
      from: params.from,
      to: params.to,
    };
    for (const [key, value] of Object.entries(optionalParams)) {
      if (value) httpParams = httpParams.set(key, value);
    }
    return this.http.get<ApiResponse<PageView<PostView>>>(`${this.baseUrl}/admin/posts`, {
      params: httpParams,
    });
  }

  getAdminPost(postId: string): Observable<ApiResponse<PostView>> {
    return this.http.get<ApiResponse<PostView>>(`${this.baseUrl}/admin/posts/${postId}`);
  }

  getPostStatistics(params: GetPostStatisticsParams): Observable<ApiResponse<PostStatisticsView>> {
    const httpParams = new HttpParams()
      .set('from', params.from)
      .set('to', params.to)
      .set('bucket', params.bucket)
      .set('timeZone', params.timeZone ?? 'Asia/Ho_Chi_Minh');
    return this.http.get<ApiResponse<PostStatisticsView>>(
      `${this.baseUrl}/admin/posts/statistics`,
      { params: httpParams },
    );
  }

  listAdminPostHistories(
    params: ListAdminPostHistoriesParams = {},
  ): Observable<ApiResponse<PageView<PostHistoryView>>> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 20));
    const optionalParams: Record<string, string | null | undefined> = {
      postId: params.postId,
      postAuthorUserId: params.postAuthorUserId,
      actorUserId: params.actorUserId,
      actorType: params.actorType,
      action: params.action,
    };
    for (const [key, value] of Object.entries(optionalParams)) {
      if (value) httpParams = httpParams.set(key, value);
    }
    return this.http.get<ApiResponse<PageView<PostHistoryView>>>(
      `${this.baseUrl}/admin/post-histories`,
      { params: httpParams },
    );
  }

  getAdminPostHistory(historyId: string): Observable<ApiResponse<PostHistoryView>> {
    return this.http.get<ApiResponse<PostHistoryView>>(
      `${this.baseUrl}/admin/post-histories/${historyId}`,
    );
  }

  listPostTypes(): Observable<ApiResponse<PostTypeView[]>> {
    return this.http.get<ApiResponse<PostTypeView[]>>(`${this.baseUrl}/admin/post-types`);
  }

  createPostType(payload: CreatePostTypePayload): Observable<ApiResponse<PostTypeView>> {
    return this.http.post<ApiResponse<PostTypeView>>(`${this.baseUrl}/admin/post-types`, payload);
  }

  updatePostType(
    id: string,
    payload: UpdatePostTypePayload,
  ): Observable<ApiResponse<PostTypeView>> {
    return this.http.put<ApiResponse<PostTypeView>>(
      `${this.baseUrl}/admin/post-types/${id}`,
      payload,
    );
  }

  deletePostType(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/admin/post-types/${id}`);
  }

  listPostTopics(): Observable<ApiResponse<PostTopicView[]>> {
    return this.http.get<ApiResponse<PostTopicView[]>>(`${this.baseUrl}/admin/post-topics`);
  }

  createPostTopic(payload: CreatePostTopicPayload): Observable<ApiResponse<PostTopicView>> {
    return this.http.post<ApiResponse<PostTopicView>>(`${this.baseUrl}/admin/post-topics`, payload);
  }

  updatePostTopic(
    id: string,
    payload: UpdatePostTopicPayload,
  ): Observable<ApiResponse<PostTopicView>> {
    return this.http.put<ApiResponse<PostTopicView>>(
      `${this.baseUrl}/admin/post-topics/${id}`,
      payload,
    );
  }

  deletePostTopic(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/admin/post-topics/${id}`);
  }
}
