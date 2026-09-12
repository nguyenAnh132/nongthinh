import { PostMediaType, PostReactionType } from '../../../../core/api/post-api.service';

export type PostCategory = string;

export type CommunityFilter =
  | 'HOME'
  | 'MINE'
  | 'SAVED'
  | 'GROUPS'
  | 'QUESTION'
  | 'MARKET'
  | 'EVENTS';

export interface CommunityAuthor {
  id: string;
  name: string;
  avatarUrl: string | null;
  location: string;
  roleLabel?: string;
  verified?: boolean;
}

export interface CommunityComment {
  id: string;
  parentCommentId: string | null;
  author: CommunityAuthor;
  content: string;
  createdAt: string;
  updatedAt: string;
  edited: boolean;
  replies: CommunityComment[];
}

export interface CommunityPost {
  id: string;
  author: CommunityAuthor;
  content: string;
  category: PostCategory | null;
  categoryLabel?: string;
  topic: string;
  createdAt: string;
  imageUrls: string[];
  videoUrl?: string | null;
  reactionCounts: Record<PostReactionType, number>;
  reactionTotal: number;
  reactionVersion: number;
  commentVersion: number;
  currentReaction: PostReactionType | null;
  reactionPending: boolean;
  reactionError: string;
  reactionMutationId: number;
  shares: number;
  sharePending: boolean;
  shareError: string;
  shareMutationId: number;
  reported: boolean;
  reportPending: boolean;
  reportError: string;
  deletePending: boolean;
  deleteError: string;
  saved: boolean;
  bookmarkPending: boolean;
  bookmarkError: string;
  bookmarkMutationId: number;
  comments: CommunityComment[];
  commentRootTotal: number;
  commentsPage: number;
  commentsHasNext: boolean;
  commentsLoaded: boolean;
  commentsLoading: boolean;
  commentsError: string;
  commentMutationPending: boolean;
  commentMutationError: string;
  commentCreateVersion: number;
  commentEditVersion: number;
}

export interface NewCommunityPost {
  content: string;
  postTypeId: string | null;
  topicId: string | null;
  location: string;
  cropTypeIds: string[];
  mediaFiles: Array<{
    file: File;
    mediaType: PostMediaType;
  }>;
}
