package com.nongthinh.post_service.domain.post.valueobject;

import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredText;

public record PostContent(String value) {
    public static final int MAX_LENGTH = 2_000;

    public PostContent {
        value = requiredText(value, MAX_LENGTH, "content");
    }
}
