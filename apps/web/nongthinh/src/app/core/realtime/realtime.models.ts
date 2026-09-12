import { PostReactionType } from '../api/post-api.service';

export interface ReactionMetrics {
  postId: string;
  reactionCounts: Record<PostReactionType, number>;
  reactionTotal: number;
  reactionVersion: number;
  currentReaction?: PostReactionType | null;
}
export interface CommentMetrics {
  postId: string;
  commentRootTotal: number;
  commentTotal: number;
  commentVersion: number;
}
export interface PostEngagement extends ReactionMetrics, CommentMetrics {}
export interface RealtimeEvent {
  eventId: string;
  eventType: string;
  schemaVersion: number;
  occurredAt: string;
  postId?: string;
  version: number;
  payload: Partial<PostEngagement> & { unreadCount?: number };
}
export interface InAppNotification {
  id: string;
  actorUserId: string;
  type:
    | 'POST_REACTION'
    | 'POST_COMMENT'
    | 'COMMENT_REPLY'
    | 'REPORT_RESOLVED'
    | 'REPORT_REJECTED'
    | 'POST_HIDDEN'
    | 'POST_DELETED'
    | 'POST_REPORT_REJECTED';
  entityType: string;
  entityId: string;
  readAt: string | null;
  createdAt: string;
}
export interface NotificationPage {
  items: InAppNotification[];
  page: number;
  size: number;
  hasNext: boolean;
}
export interface NotificationState {
  version: number;
  unreadCount: number;
}
export interface RealtimeFeatures { notifications: boolean; postEngagement: boolean }
