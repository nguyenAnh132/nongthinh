package com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.command.AiModelDiseaseMappingCommand;
import com.nongthinh.agri_catalog_service.application.view.AiModelDiseaseMappingView;

public interface UpdateAiModelDiseaseMappingUseCase {
    AiModelDiseaseMappingView execute(UUID id, AiModelDiseaseMappingCommand command);
}
