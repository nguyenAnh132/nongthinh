package com.nongthinh.agri_catalog_service.infra.persistence.aimodeldiseasemapping;

import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.domain.aimodeldiseasemapping.AiModelDiseaseMapping;

@Component
public class AiModelDiseaseMappingPersistenceMapper {

    public AiModelDiseaseMapping toDomain(JpaAiModelDiseaseMappingEntity entity) {
        return AiModelDiseaseMapping.reconstruct(
                entity.getId(),
                entity.getModelVersionClassId(),
                entity.getCropTypeId(),
                entity.getDiseaseId(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy());
    }

    public JpaAiModelDiseaseMappingEntity toEntity(AiModelDiseaseMapping mapping) {
        return JpaAiModelDiseaseMappingEntity.builder()
                .id(mapping.getId())
                .modelVersionClassId(mapping.getModelVersionClassId())
                .cropTypeId(mapping.getCropTypeId())
                .diseaseId(mapping.getDiseaseId())
                .createdAt(mapping.getCreatedAt())
                .createdBy(mapping.getCreatedBy())
                .updatedAt(mapping.getUpdatedAt())
                .updatedBy(mapping.getUpdatedBy())
                .build();
    }
}
