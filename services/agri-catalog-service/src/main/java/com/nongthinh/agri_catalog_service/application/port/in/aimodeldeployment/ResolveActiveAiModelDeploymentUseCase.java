package com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.AiModelDeploymentResolutionView;

public interface ResolveActiveAiModelDeploymentUseCase {
    AiModelDeploymentResolutionView execute(UUID cropTypeId);
}
