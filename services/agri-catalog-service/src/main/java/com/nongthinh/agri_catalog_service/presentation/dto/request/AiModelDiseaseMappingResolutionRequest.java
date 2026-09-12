package com.nongthinh.agri_catalog_service.presentation.dto.request;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AiModelDiseaseMappingResolutionRequest(
        @NotNull(message = "AI_MODEL_VERSION_REQUIRED")
        UUID modelVersionId,

        @NotNull(message = "AI_MODEL_DISEASE_MAPPING_CROP_TYPE_ID_REQUIRED")
        UUID cropTypeId,

        @NotEmpty(message = "INVALID_REQUEST_PARAMETER")
        List<@NotBlank(message = "INVALID_REQUEST_PARAMETER") String> classCodes
) {
}
