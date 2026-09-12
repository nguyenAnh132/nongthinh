package com.nongthinh.agri_catalog_service.application.port.in.aimodelversion;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.command.AiModelVersionCreationCommand;
import com.nongthinh.agri_catalog_service.application.view.AiModelVersionView;

public interface CreateAiModelVersionUseCase {
    AiModelVersionView execute(UUID modelId, AiModelVersionCreationCommand command);
}
