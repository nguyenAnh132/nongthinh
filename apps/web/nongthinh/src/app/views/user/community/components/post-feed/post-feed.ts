import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PostReactionType, PostReportReason } from '../../../../../core/api/post-api.service';
import { CommunityPost, PostCategory } from '../../models/community.models';
import { PostCard } from '../post-card/post-card';

export interface CommunityReactionEvent {
  postId: string;
  reactionType: PostReactionType | null;
}

export interface CommunityCommentsRequestEvent {
  postId: string;
  append: boolean;
}

export interface CommunityCommentCreateEvent {
  postId: string;
  content: string;
  parentCommentId: string | null;
}

export interface CommunityCommentUpdateEvent {
  postId: string;
  commentId: string;
  content: string;
}

export interface CommunityCommentDeleteEvent {
  postId: string;
  commentId: string;
}

export interface CommunityPostReportEvent {
  postId: string;
  reason: PostReportReason;
  reasonDetail: string | null;
}

@Component({
  selector: 'app-post-feed',
  standalone: true,
  imports: [PostCard],
  templateUrl: './post-feed.html',
  styleUrl: './post-feed.scss',
})
export class PostFeed {
  @Input() posts: CommunityPost[] = [];
  @Input() currentUserId = '';
  @Input() currentUserName = '';
  @Input() currentUserAvatarUrl: string | null = null;
  @Input() interactionsEnabled = false;
  @Input() detailMode = false;
  @Output() detailRequested = new EventEmitter<string>();
  @Output() reactionUsersRequested = new EventEmitter<string>();
  @Input() emptyTitle = 'Chưa có bài viết phù hợp';
  @Input() emptyDescription = 'Hãy thử một mục khác hoặc bắt đầu chia sẻ với cộng đồng.';
  @Input() showEmptyAction = true;
  @Output() reactionChanged = new EventEmitter<CommunityReactionEvent>();
  @Output() commentsRequested = new EventEmitter<CommunityCommentsRequestEvent>();
  @Output() commentCreated = new EventEmitter<CommunityCommentCreateEvent>();
  @Output() commentUpdated = new EventEmitter<CommunityCommentUpdateEvent>();
  @Output() commentDeleted = new EventEmitter<CommunityCommentDeleteEvent>();
  @Output() bookmarkToggled = new EventEmitter<string>();
  @Output() shareRequested = new EventEmitter<string>();
  @Output() reportRequested = new EventEmitter<CommunityPostReportEvent>();
  @Output() deleteRequested = new EventEmitter<string>();
  @Output() startPost = new EventEmitter<PostCategory | null>();
}
