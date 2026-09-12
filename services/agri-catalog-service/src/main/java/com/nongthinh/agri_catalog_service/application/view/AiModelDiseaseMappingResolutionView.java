package com.nongthinh.agri_catalog_service.application.view;

import java.time.Instant;
import java.util.UUID;

public record AiModelDiseaseMappingResolutionView(
        String classCode,
        UUID diseaseId,
        String displayName,
        Instant catalogUpdatedAt
) {
}
