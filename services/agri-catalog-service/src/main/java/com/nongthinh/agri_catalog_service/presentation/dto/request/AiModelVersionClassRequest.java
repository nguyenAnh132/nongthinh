package com.nongthinh.agri_catalog_service.presentation.dto.request;

import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelClassKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AiModelVersionClassRequest(
        @PositiveOrZero(message = "AI_MODEL_VERSION_CLASS_INDEX_INVALID")
        int classIndex,

        @NotBlank(message = "AI_MODEL_VERSION_CLASS_CODE_REQUIRED")
        @Size(max = 100, message = "AI_MODEL_VERSION_CLASS_CODE_TOO_LONG")
        String classCode,

        @NotBlank(message = "AI_MODEL_VERSION_CLASS_DISPLAY_NAME_REQUIRED")
        @Size(max = 255, message = "AI_MODEL_VERSION_CLASS_DISPLAY_NAME_TOO_LONG")
        String displayName,

        @NotNull(message = "AI_MODEL_VERSION_CLASS_KIND_REQUIRED")
        AiModelClassKind classKind
) {
}
