package com.nongthinh.location_service.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProvinceCreationRequest(

    @NotBlank(message = "PROVINCE_ID_REQUIRED")
    @Size(max = 10, message = "PROVINCE_ID_TOO_LONG")
    String id,

    @NotBlank(message = "PROVINCE_CODE_REQUIRED")
    @Size(max = 20, message = "PROVINCE_CODE_TOO_LONG")
    String code,

    @NotBlank(message = "PROVINCE_NAME_REQUIRED")
    @Size(max = 255, message = "PROVINCE_NAME_TOO_LONG")
    String name
) {
}
