package com.nongthinh.agri_catalog_service.application.model;

import java.time.Instant;
import java.util.UUID;

public record ResolvedAiModelDiseaseMapping(
        String classCode,
        UUID diseaseId,
        String displayName,
        Instant catalogUpdatedAt
) {
}
