package com.nongthinh.profile_service.presentation.dto.request.admin;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BrandProfileStatusUpdateRequest(

    @NotBlank(message = "PROFILE_STATUS_INVALID")
    @Pattern(
            regexp = "^(UNDER_REVIEW|NEEDS_REVISION|ACTIVE|READY_FOR_FINAL_REVIEW|PENDING_APPROVAL|REJECTED|LOCKED|DISABLED|DELETED)$",
            message = "PROFILE_STATUS_INVALID"
    )
    String status,

    @Size(max = 1000, message = "REJECTION_REASON_TOO_LONG")
    String rejectionReason,

    UUID actorUserId
) {
}
