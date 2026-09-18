package com.nongthinh.file_service.domain.file;

import java.util.Objects;
import java.util.Set;

public record FileUploadPolicy(long maxSizeBytes, Set<String> allowedContentTypes) {
    public FileUploadPolicy {
        if (maxSizeBytes <= 0) {
            throw new IllegalArgumentException("Upload size limit must be positive");
        }
        allowedContentTypes = Set.copyOf(Objects.requireNonNull(allowedContentTypes, "allowedContentTypes"));
    }
}
