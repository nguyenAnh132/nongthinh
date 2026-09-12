package com.nongthinh.rice_disease_diagnosis_service.application.model;

import java.time.Instant;
import java.util.UUID;

public record ResolvedDiseaseMapping(
        String classCode,
        UUID diseaseId,
        String displayName,
        Instant catalogUpdatedAt
) {
}
