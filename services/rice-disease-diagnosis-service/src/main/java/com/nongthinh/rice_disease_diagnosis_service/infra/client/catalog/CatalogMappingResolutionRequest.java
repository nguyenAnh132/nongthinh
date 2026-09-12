package com.nongthinh.rice_disease_diagnosis_service.infra.client.catalog;

import java.util.List;
import java.util.UUID;

public record CatalogMappingResolutionRequest(
        UUID modelVersionId,
        UUID cropTypeId,
        List<String> classCodes
) {
}
