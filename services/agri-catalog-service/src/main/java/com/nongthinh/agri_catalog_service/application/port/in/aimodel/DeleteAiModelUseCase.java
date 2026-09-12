package com.nongthinh.agri_catalog_service.application.port.in.aimodel;

import java.util.UUID;

public interface DeleteAiModelUseCase {

    void execute(UUID id);
}
