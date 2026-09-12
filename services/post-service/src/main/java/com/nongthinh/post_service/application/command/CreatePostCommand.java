package com.nongthinh.post_service.application.command;

import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.util.Set;
import java.util.UUID;

public record CreatePostCommand(
        UUID postTypeId,
        UUID topicId,
        String content,
        String locationText,
        PostVisibility visibility,
        PostStatus status,
        Set<UUID> cropTypeIds
) {
}
