package com.nongthinh.agri_catalog_service.application.port.in.aimodelversion;

import java.util.List;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.AiModelVersionView;

public interface ListAiModelVersionsUseCase {
    List<AiModelVersionView> execute(UUID modelId);
}
