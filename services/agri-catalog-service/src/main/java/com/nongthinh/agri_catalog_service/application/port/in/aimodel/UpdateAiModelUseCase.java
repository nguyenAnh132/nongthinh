package com.nongthinh.agri_catalog_service.application.port.in.aimodel;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.command.AiModelUpdateCommand;
import com.nongthinh.agri_catalog_service.application.view.AiModelView;

public interface UpdateAiModelUseCase {

    AiModelView execute(UUID id, AiModelUpdateCommand command);
}
