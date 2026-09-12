package com.nongthinh.post_service.domain.post.valueobject;

import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredId;

import java.util.UUID;

public record PostTopicId(UUID value) {
    public PostTopicId {
        requiredId(value, "postTopicId");
    }
}
