package com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.AiModelDiseaseMappingView;

public interface GetAiModelDiseaseMappingUseCase {
    AiModelDiseaseMappingView execute(UUID id);
}
