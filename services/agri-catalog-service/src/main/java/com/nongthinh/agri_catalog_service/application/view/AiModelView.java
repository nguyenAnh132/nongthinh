package com.nongthinh.agri_catalog_service.application.view;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.aimodel.AiModel;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelStatus;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelTaskType;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.CropCoverageType;

public record AiModelView(
        UUID id,
        String code,
        String name,
        String description,
        AiModelTaskType taskType,
        CropCoverageType cropCoverageType,
        Set<UUID> cropTypeIds,
        AiModelStatus status,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy
) {
    public static AiModelView from(AiModel aiModel) {
        return new AiModelView(
                aiModel.getId(),
                aiModel.getCode(),
                aiModel.getName(),
                aiModel.getDescription(),
                aiModel.getTaskType(),
                aiModel.getCropCoverageType(),
                aiModel.getCropTypeIds(),
                aiModel.getStatus(),
                aiModel.getCreatedAt(),
                aiModel.getCreatedBy(),
                aiModel.getUpdatedAt(),
                aiModel.getUpdatedBy()
        );
    }
}
