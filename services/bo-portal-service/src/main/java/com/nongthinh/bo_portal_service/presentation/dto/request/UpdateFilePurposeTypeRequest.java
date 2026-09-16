package com.nongthinh.bo_portal_service.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateFilePurposeTypeRequest(@NotNull(message = "FILE_TYPE_ENABLED_REQUIRED") Boolean enabled) {
}
