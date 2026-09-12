package com.nongthinh.post_service.application.service;

import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.repository.CommentRepository;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.comment.Comment;
import com.nongthinh.post_service.domain.comment.valueobject.CommentStatus;
import com.nongthinh.post_service.domain.exception.BusinessException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostCommentUseCaseSupport {
    private final CommentRepository repository;
    private final CurrentUserProvider currentUserProvider;

    public Comment requireCommentForUpdate(UUID postId, UUID commentId) {
        if (postId == null || commentId == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        return repository.findByIdAndPostIdForUpdate(commentId, postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }

    public Comment requireReplyParent(UUID postId, UUID parentCommentId) {
        Comment parent = repository.findByIdAndPostId(parentCommentId, postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        if (parent.getParentCommentId() != null) {
            throw new BusinessException(ErrorCode.COMMENT_REPLY_DEPTH_EXCEEDED);
        }
        if (parent.getStatus() != CommentStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        return parent;
    }

    public String content(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.COMMENT_CONTENT_REQUIRED);
        }
        if (value.trim().length() > Comment.CONTENT_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.COMMENT_CONTENT_TOO_LONG);
        }
        return value;
    }

    public UUID currentUserId() {
        return currentUserProvider.getCurrentUserId();
    }

    public void assertOwner(Comment comment, UUID actorId) {
        if (!comment.getAuthorUserId().equals(actorId)) {
            throw new BusinessException(ErrorCode.COMMENT_ACCESS_DENIED);
        }
    }

    public void assertEditable(Comment comment) {
        if (comment.getStatus() == CommentStatus.HIDDEN) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_EDITABLE);
        }
    }
}
