package com.nongthinh.agri_catalog_service.application.command;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.valueobject.AiModelDeploymentScope;

public record AiModelDeploymentCreationCommand(
        UUID modelVersionId,
        AiModelDeploymentScope deploymentScope,
        UUID cropTypeId,
        int priority
) {
}
