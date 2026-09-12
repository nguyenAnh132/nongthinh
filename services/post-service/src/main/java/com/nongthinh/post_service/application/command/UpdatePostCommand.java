package com.nongthinh.post_service.application.command;

import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.util.Set;
import java.util.UUID;

public record UpdatePostCommand(
        UUID postTypeId,
        UUID topicId,
        String content,
        String locationText,
        PostVisibility visibility,
        Set<UUID> cropTypeIds
) {
}
