package com.nongthinh.post_service.application.view;

import java.util.UUID;

public record DeletedPostCommentView(UUID postId, UUID deletedCommentId,
        long commentRootTotal, long commentTotal, long commentVersion) {
    public static DeletedPostCommentView from(UUID id, PostEngagementView metrics) {
        return new DeletedPostCommentView(metrics.postId(), id, metrics.commentRootTotal(),
                metrics.commentTotal(), metrics.commentVersion());
    }
}
