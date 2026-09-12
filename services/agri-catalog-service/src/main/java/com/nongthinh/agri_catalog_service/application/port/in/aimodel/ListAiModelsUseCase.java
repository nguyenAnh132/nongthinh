package com.nongthinh.agri_catalog_service.application.port.in.aimodel;

import java.util.List;
import com.nongthinh.agri_catalog_service.application.view.AiModelView;

public interface ListAiModelsUseCase {

    List<AiModelView> execute();
}
