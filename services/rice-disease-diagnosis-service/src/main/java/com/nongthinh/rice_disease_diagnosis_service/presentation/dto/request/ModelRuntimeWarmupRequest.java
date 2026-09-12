package com.nongthinh.rice_disease_diagnosis_service.presentation.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record ModelRuntimeWarmupRequest(
        @NotNull UUID modelVersionId,
        @NotNull UUID artifactFileId,
        @NotBlank @Pattern(regexp = "(?i)^[a-f0-9]{64}$") String artifactSha256,
        @Positive int inputWidth,
        @Positive int inputHeight
) {
}
