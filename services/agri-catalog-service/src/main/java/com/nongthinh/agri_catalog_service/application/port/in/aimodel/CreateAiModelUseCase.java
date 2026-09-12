package com.nongthinh.agri_catalog_service.application.port.in.aimodel;

import com.nongthinh.agri_catalog_service.application.command.AiModelCreationCommand;
import com.nongthinh.agri_catalog_service.application.view.AiModelView;

public interface CreateAiModelUseCase {

    AiModelView execute(AiModelCreationCommand command);
}
