package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.domain.post.PostTopic;
import java.time.Instant;
import java.util.UUID;

public record PostTopicView(UUID id, String name, String slug, String description, int displayOrder,
                            boolean active, Instant createdAt, Instant updatedAt) {
    public static PostTopicView from(PostTopic value) {
        return new PostTopicView(value.getId(), value.getName(), value.getSlug(), value.getDescription(),
                value.getDisplayOrder(), value.isActive(), value.getCreatedAt(), value.getUpdatedAt());
    }
}
