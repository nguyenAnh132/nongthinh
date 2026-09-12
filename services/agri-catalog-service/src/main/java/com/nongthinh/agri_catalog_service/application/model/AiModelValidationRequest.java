package com.nongthinh.agri_catalog_service.application.model;

import java.util.List;
import java.util.UUID;

public record AiModelValidationRequest(
        UUID modelVersionId,
        UUID artifactFileId,
        String artifactSha256,
        int inputWidth,
        int inputHeight,
        List<AiModelValidationClass> classes
) {
}
