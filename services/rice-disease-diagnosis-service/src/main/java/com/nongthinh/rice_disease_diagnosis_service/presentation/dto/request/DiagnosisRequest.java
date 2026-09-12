package com.nongthinh.rice_disease_diagnosis_service.presentation.dto.request;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DiagnosisRequest(
        @NotNull UUID cropTypeId,
        @NotNull @Size(min = 1, max = 5) List<@NotNull UUID> fileIds
) {
}
