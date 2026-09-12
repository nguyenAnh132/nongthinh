package com.nongthinh.rice_disease_diagnosis_service.infra.client.catalog;

import java.time.Instant;
import java.util.UUID;

public record CatalogDiseaseMappingDto(
        String classCode,
        UUID diseaseId,
        String displayName,
        Instant catalogUpdatedAt
) {
}
