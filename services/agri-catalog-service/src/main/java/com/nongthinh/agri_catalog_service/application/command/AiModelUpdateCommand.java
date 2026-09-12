package com.nongthinh.agri_catalog_service.application.command;

import java.util.Set;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.CropCoverageType;

public record AiModelUpdateCommand(
        String name,
        String description,
        CropCoverageType cropCoverageType,
        Set<UUID> cropTypeIds
) {
}
