package com.nongthinh.profile_service.presentation.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProfileStatusUpdateRequest(

    @NotBlank(message = "PROFILE_STATUS_INVALID")
    @Pattern(regexp = "^(ACTIVE|LOCKED|DISABLED|DELETED)$", message = "PROFILE_STATUS_INVALID")
    String status
) {
}
