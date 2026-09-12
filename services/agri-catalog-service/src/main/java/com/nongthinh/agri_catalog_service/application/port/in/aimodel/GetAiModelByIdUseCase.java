package com.nongthinh.agri_catalog_service.application.port.in.aimodel;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.AiModelView;

public interface GetAiModelByIdUseCase {

    AiModelView execute(UUID id);
}
