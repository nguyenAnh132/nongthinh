package com.nongthinh.agri_catalog_service.presentation.dto.request;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AiModelVersionCreationRequest(
        @NotBlank(message = "AI_MODEL_VERSION_REQUIRED")
        @Size(max = 128, message = "AI_MODEL_VERSION_TOO_LONG")
        String version,

        @NotNull(message = "AI_MODEL_VERSION_ARTIFACT_FILE_ID_REQUIRED")
        UUID artifactFileId,

        @NotBlank(message = "AI_MODEL_VERSION_ARTIFACT_SHA256_REQUIRED")
        @Pattern(regexp = "(?i)^[a-f0-9]{64}$", message = "AI_MODEL_VERSION_ARTIFACT_SHA256_INVALID")
        String artifactSha256,

        @Positive(message = "AI_MODEL_VERSION_INPUT_WIDTH_INVALID")
        int inputWidth,

        @Positive(message = "AI_MODEL_VERSION_INPUT_HEIGHT_INVALID")
        int inputHeight,

        @NotNull(message = "AI_MODEL_VERSION_CLASSES_REQUIRED")
        @Size(min = 1, message = "AI_MODEL_VERSION_CLASSES_REQUIRED")
        List<@Valid AiModelVersionClassRequest> classes
) {
}
