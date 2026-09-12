package com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping;

import java.util.List;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.AiModelDiseaseMappingResolutionView;

public interface ResolveAiModelDiseaseMappingsUseCase {
    List<AiModelDiseaseMappingResolutionView> execute(
            UUID modelVersionId, UUID cropTypeId, List<String> classCodes);
}
