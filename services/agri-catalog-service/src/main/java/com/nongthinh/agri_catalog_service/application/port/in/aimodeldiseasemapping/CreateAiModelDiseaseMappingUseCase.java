package com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping;

import com.nongthinh.agri_catalog_service.application.command.AiModelDiseaseMappingCommand;
import com.nongthinh.agri_catalog_service.application.view.AiModelDiseaseMappingView;

public interface CreateAiModelDiseaseMappingUseCase {
    AiModelDiseaseMappingView execute(AiModelDiseaseMappingCommand command);
}
