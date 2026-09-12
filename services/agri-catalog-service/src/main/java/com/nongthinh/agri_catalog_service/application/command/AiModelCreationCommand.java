package com.nongthinh.agri_catalog_service.application.command;

import java.util.Set;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelTaskType;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.CropCoverageType;

public record AiModelCreationCommand(
        String code,
        String name,
        String description,
        AiModelTaskType taskType,
        CropCoverageType cropCoverageType,
        Set<UUID> cropTypeIds
) {
}
