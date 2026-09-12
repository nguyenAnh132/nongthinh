package com.nongthinh.post_service.application.model;

import com.nongthinh.post_service.domain.comment.Comment;
import java.util.List;

public record CommentThread(Comment comment, List<Comment> replies) {
    public CommentThread {
        replies = List.copyOf(replies);
    }
}
