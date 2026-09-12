package com.nongthinh.profile_service.presentation.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectBrandEarlyRequest(

    @NotBlank(message = "REJECTION_REASON_REQUIRED")
    @Size(max = 1000, message = "REJECTION_REASON_TOO_LONG")
    String rejectionReason
) {
}
