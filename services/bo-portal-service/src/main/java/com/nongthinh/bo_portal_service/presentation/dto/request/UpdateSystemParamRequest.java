package com.nongthinh.bo_portal_service.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateSystemParamRequest(
        @NotBlank(message = "SYSTEM_PARAM_VALUE_REQUIRED")
        String value,
        String description
) {
}
