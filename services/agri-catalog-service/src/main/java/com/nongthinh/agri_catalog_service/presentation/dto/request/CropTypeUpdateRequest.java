package com.nongthinh.agri_catalog_service.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CropTypeUpdateRequest(
        @NotBlank(message = "CROP_TYPE_NAME_REQUIRED")
        @Size(max = 150, message = "CROP_TYPE_NAME_TOO_LONG")
        String name,

        @Size(max = 10000, message = "CROP_TYPE_DESCRIPTION_TOO_LONG")
        String description,

        @NotNull(message = "CROP_TYPE_ACTIVE_REQUIRED")
        Boolean active
) {
}

