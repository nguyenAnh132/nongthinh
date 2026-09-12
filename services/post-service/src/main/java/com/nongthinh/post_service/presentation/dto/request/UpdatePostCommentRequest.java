package com.nongthinh.post_service.presentation.dto.request;

import com.nongthinh.post_service.domain.comment.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePostCommentRequest(
        @NotBlank(message = "COMMENT_CONTENT_REQUIRED")
        @Size(max = Comment.CONTENT_MAX_LENGTH, message = "COMMENT_CONTENT_TOO_LONG")
        String content
) {
}
