package com.nongthinh.agri_catalog_service.infra.persistence.aimodeldeployment;

import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.AiModelDeployment;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.valueobject.AiModelDeploymentScope;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.valueobject.AiModelDeploymentStatus;

@Component
public class AiModelDeploymentPersistenceMapper {

    public AiModelDeployment toDomain(JpaAiModelDeploymentEntity entity) {
        return AiModelDeployment.reconstruct(
                entity.getId(),
                entity.getModelVersionId(),
                entity.getCropTypeId(),
                AiModelDeploymentScope.valueOf(entity.getDeploymentScope()),
                entity.getPriority(),
                AiModelDeploymentStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getActivatedAt(),
                entity.getActivatedBy()
        );
    }

    public JpaAiModelDeploymentEntity toEntity(AiModelDeployment deployment) {
        return JpaAiModelDeploymentEntity.builder()
                .id(deployment.getId())
                .modelVersionId(deployment.getModelVersionId())
                .cropTypeId(deployment.getCropTypeId())
                .deploymentScope(deployment.getDeploymentScope().name())
                .priority(deployment.getPriority())
                .status(deployment.getStatus().name())
                .createdAt(deployment.getCreatedAt())
                .createdBy(deployment.getCreatedBy())
                .activatedAt(deployment.getActivatedAt())
                .activatedBy(deployment.getActivatedBy())
                .build();
    }
}
