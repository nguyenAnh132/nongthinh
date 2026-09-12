package com.nongthinh.agri_catalog_service.application.port.out;

import com.nongthinh.agri_catalog_service.application.model.AiModelRuntimeWarmupRequest;

public interface AiModelRuntimeWarmer {
    void warm(AiModelRuntimeWarmupRequest request);
}
