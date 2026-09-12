package com.nongthinh.rice_disease_diagnosis_service.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ModelClassManifestRequest(
        @PositiveOrZero int classIndex,
        @NotBlank String classCode,
        @NotBlank String classKind
) {
}
