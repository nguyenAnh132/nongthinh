package com.nongthinh.post_service.domain.interaction;

import static com.nongthinh.post_service.domain.shared.DomainValidation.required;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredId;

import java.time.Instant;
import java.util.UUID;

public record Bookmark(UUID postId, UUID userId, Instant createdAt) {
    public Bookmark {
        requiredId(postId, "postId");
        requiredId(userId, "userId");
        required(createdAt, "createdAt");
    }
}
