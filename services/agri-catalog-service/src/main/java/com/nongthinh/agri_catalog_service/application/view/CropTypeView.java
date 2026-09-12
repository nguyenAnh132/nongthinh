package com.nongthinh.agri_catalog_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.croptype.CropType;

public record CropTypeView(
        UUID id,
        String code,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy
) {
    public static CropTypeView from(CropType cropType) {
        return new CropTypeView(
                cropType.getId(),
                cropType.getCode(),
                cropType.getName(),
                cropType.getDescription(),
                cropType.isActive(),
                cropType.getCreatedAt(),
                cropType.getCreatedBy(),
                cropType.getUpdatedAt(),
                cropType.getUpdatedBy()
        );
    }
}

