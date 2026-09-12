package com.nongthinh.agri_catalog_service.infra.persistence.aimodelversion;

import java.util.List;
import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersionClass;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelClassKind;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelVersionStatus;

@Component
public class AiModelVersionPersistenceMapper {

    public AiModelVersion toDomain(
            JpaAiModelVersionEntity entity,
            List<JpaAiModelVersionClassEntity> classEntities) {
        return AiModelVersion.reconstruct(
                entity.getId(),
                entity.getModelId(),
                entity.getVersion(),
                entity.getArtifactFileId(),
                entity.getArtifactSha256(),
                entity.getInputWidth(),
                entity.getInputHeight(),
                classEntities.stream().map(this::toDomain).toList(),
                AiModelVersionStatus.valueOf(entity.getStatus()),
                entity.getValidationReport(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getValidatedAt(),
                entity.getValidatedBy(),
                entity.getRetiredAt(),
                entity.getRetiredBy()
        );
    }

    public JpaAiModelVersionEntity toEntity(AiModelVersion version) {
        return JpaAiModelVersionEntity.builder()
                .id(version.getId())
                .modelId(version.getModelId())
                .version(version.getVersion())
                .artifactFileId(version.getArtifactFileId())
                .artifactSha256(version.getArtifactSha256())
                .inputWidth(version.getInputWidth())
                .inputHeight(version.getInputHeight())
                .status(version.getStatus().name())
                .validationReport(version.getValidationReport())
                .createdAt(version.getCreatedAt())
                .createdBy(version.getCreatedBy())
                .validatedAt(version.getValidatedAt())
                .validatedBy(version.getValidatedBy())
                .retiredAt(version.getRetiredAt())
                .retiredBy(version.getRetiredBy())
                .build();
    }

    public AiModelVersionClass toDomain(JpaAiModelVersionClassEntity entity) {
        return AiModelVersionClass.reconstruct(
                entity.getId(),
                entity.getModelVersionId(),
                entity.getClassIndex(),
                entity.getClassCode(),
                entity.getDisplayName(),
                AiModelClassKind.valueOf(entity.getClassKind())
        );
    }

    public JpaAiModelVersionClassEntity toEntity(AiModelVersionClass item) {
        return JpaAiModelVersionClassEntity.builder()
                .id(item.getId())
                .modelVersionId(item.getModelVersionId())
                .classIndex(item.getClassIndex())
                .classCode(item.getClassCode())
                .displayName(item.getDisplayName())
                .classKind(item.getClassKind().name())
                .build();
    }
}
