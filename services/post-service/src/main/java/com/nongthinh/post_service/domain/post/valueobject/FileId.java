package com.nongthinh.post_service.domain.post.valueobject;

import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredId;

import java.util.UUID;

public record FileId(UUID value) {
    public FileId {
        requiredId(value, "fileId");
    }
}
