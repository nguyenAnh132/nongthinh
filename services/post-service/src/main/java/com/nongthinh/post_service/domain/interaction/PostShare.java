package com.nongthinh.post_service.domain.interaction;

import static com.nongthinh.post_service.domain.shared.DomainValidation.required;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredId;

import java.time.Instant;
import java.util.UUID;

public record PostShare(UUID id, UUID postId, UUID sharedByUserId, Instant createdAt) {
    public PostShare {
        requiredId(id, "shareId");
        requiredId(postId, "postId");
        requiredId(sharedByUserId, "sharedByUserId");
        required(createdAt, "createdAt");
    }
}
