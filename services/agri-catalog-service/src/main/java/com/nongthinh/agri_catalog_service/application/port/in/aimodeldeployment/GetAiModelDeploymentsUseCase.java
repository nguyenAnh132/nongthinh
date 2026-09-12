package com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment;

import java.util.List;
import com.nongthinh.agri_catalog_service.application.view.AiModelDeploymentView;

public interface GetAiModelDeploymentsUseCase {
    List<AiModelDeploymentView> execute();
}
