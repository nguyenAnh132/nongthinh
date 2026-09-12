package com.nongthinh.location_service.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommuneCreationRequest(

    @NotBlank(message = "PROVINCE_ID_REQUIRED")
    @Size(max = 10, message = "PROVINCE_ID_TOO_LONG")
    String provinceId,

    @NotBlank(message = "COMMUNE_CODE_REQUIRED")
    @Size(max = 20, message = "COMMUNE_CODE_TOO_LONG")
    String code,

    @NotBlank(message = "COMMUNE_NAME_REQUIRED")
    @Size(max = 255, message = "COMMUNE_NAME_TOO_LONG")
    String name
) {
}
