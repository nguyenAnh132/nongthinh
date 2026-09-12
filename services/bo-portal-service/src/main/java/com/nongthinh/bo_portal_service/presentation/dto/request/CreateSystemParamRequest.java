package com.nongthinh.bo_portal_service.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSystemParamRequest(
        @NotBlank(message = "SYSTEM_PARAM_NAME_REQUIRED")
        String name,
        @NotBlank(message = "SYSTEM_PARAM_VALUE_REQUIRED")
        String value,
        String description,
        @NotBlank(message = "SYSTEM_PARAM_DATA_TYPE_INVALID")
        String dataType,
        Long typeId
) {
}
