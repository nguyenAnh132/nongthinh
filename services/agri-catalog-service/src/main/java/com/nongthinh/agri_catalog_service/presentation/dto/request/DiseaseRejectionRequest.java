package com.nongthinh.agri_catalog_service.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DiseaseRejectionRequest(
        @NotBlank(message = "DISEASE_REJECTION_REASON_REQUIRED")
        @Size(max = 2000, message = "DISEASE_REJECTION_REASON_TOO_LONG")
        String reason
) {
}
