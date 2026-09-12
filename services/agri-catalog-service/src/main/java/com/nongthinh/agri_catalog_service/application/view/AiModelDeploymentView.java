package com.nongthinh.agri_catalog_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.AiModelDeployment;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.valueobject.AiModelDeploymentScope;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.valueobject.AiModelDeploymentStatus;

public record AiModelDeploymentView(
        UUID id,
        UUID modelVersionId,
        UUID cropTypeId,
        AiModelDeploymentScope deploymentScope,
        int priority,
        AiModelDeploymentStatus status,
        Instant createdAt,
        UUID createdBy,
        Instant activatedAt,
        UUID activatedBy
) {
    public static AiModelDeploymentView from(AiModelDeployment deployment) {
        return new AiModelDeploymentView(
                deployment.getId(), deployment.getModelVersionId(), deployment.getCropTypeId(),
                deployment.getDeploymentScope(), deployment.getPriority(), deployment.getStatus(),
                deployment.getCreatedAt(), deployment.getCreatedBy(), deployment.getActivatedAt(), deployment.getActivatedBy());
    }
}
