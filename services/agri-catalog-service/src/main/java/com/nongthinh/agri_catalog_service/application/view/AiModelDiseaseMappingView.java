package com.nongthinh.agri_catalog_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.aimodeldiseasemapping.AiModelDiseaseMapping;

public record AiModelDiseaseMappingView(
        UUID id,
        UUID modelVersionClassId,
        UUID cropTypeId,
        UUID diseaseId,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy
) {
    public static AiModelDiseaseMappingView from(AiModelDiseaseMapping mapping) {
        return new AiModelDiseaseMappingView(
                mapping.getId(),
                mapping.getModelVersionClassId(),
                mapping.getCropTypeId(),
                mapping.getDiseaseId(),
                mapping.getCreatedAt(),
                mapping.getCreatedBy(),
                mapping.getUpdatedAt(),
                mapping.getUpdatedBy());
    }
}
