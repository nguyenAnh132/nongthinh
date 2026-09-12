package com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.AiModelDeploymentView;

public interface ActivateAiModelDeploymentUseCase {
    AiModelDeploymentView execute(UUID deploymentId);
}
