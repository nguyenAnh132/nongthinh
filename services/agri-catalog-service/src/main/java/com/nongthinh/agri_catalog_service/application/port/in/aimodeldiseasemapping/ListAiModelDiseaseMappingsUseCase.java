package com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping;

import java.util.List;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.AiModelDiseaseMappingView;

public interface ListAiModelDiseaseMappingsUseCase {
    List<AiModelDiseaseMappingView> execute(UUID modelVersionId);
}
