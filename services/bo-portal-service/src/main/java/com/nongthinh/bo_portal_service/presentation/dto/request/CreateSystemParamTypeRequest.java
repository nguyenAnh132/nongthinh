package com.nongthinh.bo_portal_service.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSystemParamTypeRequest(
        @NotBlank(message = "SYSTEM_PARAM_TYPE_NAME_REQUIRED")
        @Size(max = 100, message = "SYSTEM_PARAM_TYPE_NAME_REQUIRED")
        String name,
        String description
) {
}
