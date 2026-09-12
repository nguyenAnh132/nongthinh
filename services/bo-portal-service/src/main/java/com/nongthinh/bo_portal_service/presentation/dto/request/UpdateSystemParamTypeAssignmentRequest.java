package com.nongthinh.bo_portal_service.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateSystemParamTypeAssignmentRequest(
        @NotNull(message = "SYSTEM_PARAM_TYPE_ID_REQUIRED")
        Long typeId
) {
}
