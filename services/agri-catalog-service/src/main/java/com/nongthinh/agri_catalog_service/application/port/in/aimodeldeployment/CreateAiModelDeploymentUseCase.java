package com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment;

import com.nongthinh.agri_catalog_service.application.command.AiModelDeploymentCreationCommand;
import com.nongthinh.agri_catalog_service.application.view.AiModelDeploymentView;

public interface CreateAiModelDeploymentUseCase {
    AiModelDeploymentView execute(AiModelDeploymentCreationCommand command);
}
