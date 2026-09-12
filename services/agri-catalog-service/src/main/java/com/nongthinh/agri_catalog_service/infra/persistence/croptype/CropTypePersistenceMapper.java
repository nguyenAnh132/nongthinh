package com.nongthinh.agri_catalog_service.infra.persistence.croptype;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.domain.croptype.CropType;

@Mapper(componentModel = "spring")
public interface CropTypePersistenceMapper {

    default CropType toDomain(JpaCropTypeEntity entity) {
        return CropType.reconstruct(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    default JpaCropTypeEntity toEntity(CropType cropType) {
        return JpaCropTypeEntity.builder()
                .id(cropType.getId())
                .code(cropType.getCode())
                .name(cropType.getName())
                .description(cropType.getDescription())
                .active(cropType.isActive())
                .createdAt(cropType.getCreatedAt())
                .createdBy(cropType.getCreatedBy())
                .updatedAt(cropType.getUpdatedAt())
                .updatedBy(cropType.getUpdatedBy())
                .deletedAt(cropType.getDeletedAt())
                .deletedBy(cropType.getDeletedBy())
                .build();
    }
}

