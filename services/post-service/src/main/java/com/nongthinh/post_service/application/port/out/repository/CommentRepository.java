package com.nongthinh.post_service.application.port.out.repository;

import com.nongthinh.post_service.application.model.CommentPage;
import com.nongthinh.post_service.domain.comment.Comment;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository {
    Optional<Comment> findByIdAndPostId(UUID id, UUID postId);
    Optional<Comment> findByIdAndPostIdForUpdate(UUID id, UUID postId);
    CommentPage findPublishedThreads(UUID postId, int page, int size);
    Comment save(Comment comment);
}
