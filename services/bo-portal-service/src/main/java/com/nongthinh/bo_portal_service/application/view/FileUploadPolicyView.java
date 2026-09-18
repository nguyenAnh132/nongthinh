package com.nongthinh.bo_portal_service.application.view;

import java.util.List;
import java.util.Set;

public record FileUploadPolicyView(String purpose, Long maxSizeBytes,
        Set<String> allowedContentTypes, List<FilePurposeTypeView> fileTypes) {
}
