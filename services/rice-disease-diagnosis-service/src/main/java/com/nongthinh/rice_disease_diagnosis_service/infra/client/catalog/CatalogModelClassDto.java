package com.nongthinh.rice_disease_diagnosis_service.infra.client.catalog;

import java.util.UUID;

public record CatalogModelClassDto(
        UUID id,
        int classIndex,
        String classCode,
        String displayName,
        String classKind
) {
}
