package com.nongthinh.agri_catalog_service.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CropTypeCreationRequest(
        @NotBlank(message = "CROP_TYPE_CODE_REQUIRED")
        @Size(max = 50, message = "CROP_TYPE_CODE_TOO_LONG")
        @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "CROP_TYPE_CODE_INVALID")
        String code,

        @NotBlank(message = "CROP_TYPE_NAME_REQUIRED")
        @Size(max = 150, message = "CROP_TYPE_NAME_TOO_LONG")
        String name,

        @Size(max = 10000, message = "CROP_TYPE_DESCRIPTION_TOO_LONG")
        String description
) {
}
