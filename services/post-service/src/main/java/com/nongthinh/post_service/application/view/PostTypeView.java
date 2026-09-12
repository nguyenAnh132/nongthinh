package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.domain.post.PostType;
import java.time.Instant;
import java.util.UUID;

public record PostTypeView(UUID id, String code, String name, String description, int displayOrder,
                           boolean active, Instant createdAt, Instant updatedAt) {
    public static PostTypeView from(PostType value) {
        return new PostTypeView(value.getId(), value.getCode(), value.getName(), value.getDescription(),
                value.getDisplayOrder(), value.isActive(), value.getCreatedAt(), value.getUpdatedAt());
    }
}
