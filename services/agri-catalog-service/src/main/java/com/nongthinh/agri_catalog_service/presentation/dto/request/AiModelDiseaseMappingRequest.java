package com.nongthinh.agri_catalog_service.presentation.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record AiModelDiseaseMappingRequest(
        @NotNull(message = "AI_MODEL_DISEASE_MAPPING_MODEL_VERSION_CLASS_ID_REQUIRED")
        UUID modelVersionClassId,

        @NotNull(message = "AI_MODEL_DISEASE_MAPPING_CROP_TYPE_ID_REQUIRED")
        UUID cropTypeId,

        @NotNull(message = "AI_MODEL_DISEASE_MAPPING_DISEASE_ID_REQUIRED")
        UUID diseaseId
) {
}
