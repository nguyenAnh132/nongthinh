package com.nongthinh.agri_catalog_service.application.port.out;

import com.nongthinh.agri_catalog_service.application.model.AiModelValidationRequest;
import com.nongthinh.agri_catalog_service.application.model.AiModelValidationResult;

public interface AiModelVersionValidator {

    AiModelValidationResult validate(AiModelValidationRequest request);
}
