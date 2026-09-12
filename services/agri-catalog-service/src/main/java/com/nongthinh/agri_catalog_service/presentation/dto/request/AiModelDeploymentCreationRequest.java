package com.nongthinh.agri_catalog_service.presentation.dto.request;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.valueobject.AiModelDeploymentScope;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AiModelDeploymentCreationRequest(
        @NotNull(message = "AI_MODEL_DEPLOYMENT_MODEL_VERSION_ID_REQUIRED")
        UUID modelVersionId,

        @NotNull(message = "AI_MODEL_DEPLOYMENT_SCOPE_REQUIRED")
        AiModelDeploymentScope deploymentScope,

        UUID cropTypeId,

        @PositiveOrZero(message = "AI_MODEL_DEPLOYMENT_PRIORITY_INVALID")
        int priority
) {
}
