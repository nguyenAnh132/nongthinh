package com.nongthinh.agri_catalog_service.infra.persistence.aimodel;

import java.util.Set;
import java.util.UUID;
import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.domain.aimodel.AiModel;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelStatus;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelTaskType;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.CropCoverageType;

@Mapper(componentModel = "spring")
public interface AiModelPersistenceMapper {

    default AiModel toDomain(JpaAiModelEntity entity, Set<UUID> cropTypeIds) {
        return AiModel.reconstruct(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                AiModelTaskType.valueOf(entity.getTaskType()),
                CropCoverageType.valueOf(entity.getCropCoverageType()),
                cropTypeIds,
                AiModelStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    default JpaAiModelEntity toEntity(AiModel aiModel) {
        return JpaAiModelEntity.builder()
                .id(aiModel.getId())
                .code(aiModel.getCode())
                .name(aiModel.getName())
                .description(aiModel.getDescription())
                .taskType(aiModel.getTaskType().name())
                .cropCoverageType(aiModel.getCropCoverageType().name())
                .status(aiModel.getStatus().name())
                .createdAt(aiModel.getCreatedAt())
                .createdBy(aiModel.getCreatedBy())
                .updatedAt(aiModel.getUpdatedAt())
                .updatedBy(aiModel.getUpdatedBy())
                .deletedAt(aiModel.getDeletedAt())
                .deletedBy(aiModel.getDeletedBy())
                .build();
    }
}
