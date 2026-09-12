package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.domain.interaction.Bookmark;
import java.time.Instant;
import java.util.UUID;

public record PostBookmarkView(UUID postId, UUID userId, Instant createdAt) {
    public static PostBookmarkView from(Bookmark bookmark) {
        return new PostBookmarkView(
                bookmark.postId(), bookmark.userId(), bookmark.createdAt());
    }
}
