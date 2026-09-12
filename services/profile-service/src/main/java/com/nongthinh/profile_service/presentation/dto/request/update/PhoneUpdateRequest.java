package com.nongthinh.profile_service.presentation.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneUpdateRequest(

    @NotBlank(message = "PHONE_REQUIRED")
    @Pattern(regexp = "^\\d{10}$", message = "PHONE_INVALID")
    String phone
) {
}
