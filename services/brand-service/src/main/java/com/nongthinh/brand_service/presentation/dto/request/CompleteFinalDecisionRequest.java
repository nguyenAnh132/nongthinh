package com.nongthinh.brand_service.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompleteFinalDecisionRequest(
        @NotBlank(message = "OUTCOME_INVALID")
        @Pattern(regexp = "^(APPROVED|REJECTED)$", message = "OUTCOME_INVALID")
        String outcome,

        @Size(max = 1000, message = "REJECTION_REASON_TOO_LONG")
        String rejectionReason
) {
}
