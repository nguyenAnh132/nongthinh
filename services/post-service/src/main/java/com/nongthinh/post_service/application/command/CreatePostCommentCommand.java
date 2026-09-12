package com.nongthinh.post_service.application.command;

import java.util.UUID;

public record CreatePostCommentCommand(String content, UUID parentCommentId) {
}
