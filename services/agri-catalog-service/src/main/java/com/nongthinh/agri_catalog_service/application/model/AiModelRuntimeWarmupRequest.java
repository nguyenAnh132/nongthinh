package com.nongthinh.agri_catalog_service.application.model;

import java.util.UUID;

public record AiModelRuntimeWarmupRequest(
        UUID modelVersionId,
        UUID artifactFileId,
        String artifactSha256,
        int inputWidth,
        int inputHeight
) {
}
