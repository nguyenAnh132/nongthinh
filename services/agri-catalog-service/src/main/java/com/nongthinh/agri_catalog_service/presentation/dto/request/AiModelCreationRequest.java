package com.nongthinh.agri_catalog_service.presentation.dto.request;

import java.util.Set;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelTaskType;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.CropCoverageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AiModelCreationRequest(
        @NotBlank(message = "AI_MODEL_CODE_REQUIRED")
        @Size(max = 100, message = "AI_MODEL_CODE_TOO_LONG")
        @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "AI_MODEL_CODE_INVALID")
        String code,

        @NotBlank(message = "AI_MODEL_NAME_REQUIRED")
        @Size(max = 255, message = "AI_MODEL_NAME_TOO_LONG")
        String name,

        @Size(max = 10000, message = "AI_MODEL_DESCRIPTION_TOO_LONG")
        String description,

        @NotNull(message = "AI_MODEL_TASK_TYPE_REQUIRED")
        AiModelTaskType taskType,

        @NotNull(message = "AI_MODEL_CROP_COVERAGE_TYPE_REQUIRED")
        CropCoverageType cropCoverageType,

        @NotNull(message = "AI_MODEL_CROP_TYPE_IDS_REQUIRED")
        Set<UUID> cropTypeIds
) {
}
