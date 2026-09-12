package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.domain.comment.Comment;
import com.nongthinh.post_service.domain.comment.valueobject.CommentStatus;
import java.time.Instant;
import java.util.UUID;

public record PostCommentView(
        UUID id,
        UUID postId,
        UUID parentCommentId,
        UUID authorUserId,
        String content,
        CommentStatus status,
        Instant createdAt,
        Instant updatedAt,
        long commentRootTotal,
        long commentTotal,
        long commentVersion
) {
    public PostCommentView(UUID id, UUID postId, UUID parentCommentId, UUID authorUserId,
                           String content, CommentStatus status, Instant createdAt, Instant updatedAt) {
        this(id, postId, parentCommentId, authorUserId, content, status, createdAt, updatedAt, 0, 0, 0);
    }

    public PostCommentView withMetrics(PostEngagementView metrics) {
        return new PostCommentView(id, postId, parentCommentId, authorUserId, content, status, createdAt,
                updatedAt, metrics.commentRootTotal(), metrics.commentTotal(), metrics.commentVersion());
    }
    public static PostCommentView from(Comment comment) {
        return new PostCommentView(
                comment.getId(), comment.getPostId(), comment.getParentCommentId(),
                comment.getAuthorUserId(), comment.getContent(), comment.getStatus(),
                comment.getCreatedAt(), comment.getUpdatedAt()
        );
    }
}
