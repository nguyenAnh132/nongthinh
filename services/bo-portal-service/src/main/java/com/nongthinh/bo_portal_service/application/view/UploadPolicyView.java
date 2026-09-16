package com.nongthinh.bo_portal_service.application.view;

import java.util.Set;

public record UploadPolicyView(String purpose, Long maxSizeBytes,
        Set<String> allowedContentTypes, Set<String> allowedExtensions) {
}
