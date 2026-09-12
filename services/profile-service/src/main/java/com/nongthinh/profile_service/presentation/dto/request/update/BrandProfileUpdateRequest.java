package com.nongthinh.profile_service.presentation.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.nongthinh.profile_service.presentation.validator.BrandNameConstraint;
import com.nongthinh.profile_service.presentation.validator.PersonNameConstraint;

public record BrandProfileUpdateRequest(

    @NotBlank(message = "BRAND_NAME_REQUIRED")
    @BrandNameConstraint(min = 1, max = 200, type = "Brand name", message = "BRAND_NAME_LENGTH_INVALID")
    String brandName,

    @Size(max = 20, message = "TAX_CODE_TOO_LONG")
    String taxCode,

    @Size(max = 1000, message = "DESCRIPTION_TOO_LONG")
    String description,

    @NotBlank(message = "REPRESENTATIVE_NAME_REQUIRED")
    @PersonNameConstraint(min = 1, max = 255, type = "Representative name", message = "PERSON_NAME_INVALID")
    String representativeName
) {
}
