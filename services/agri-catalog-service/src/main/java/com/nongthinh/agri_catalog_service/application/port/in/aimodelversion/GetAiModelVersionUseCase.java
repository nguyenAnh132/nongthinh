package com.nongthinh.agri_catalog_service.application.port.in.aimodelversion;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.AiModelVersionView;

public interface GetAiModelVersionUseCase {
    AiModelVersionView execute(UUID versionId);
}
