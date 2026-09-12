package com.nongthinh.brand_service.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompletePhoneVerificationRequest(
        @NotBlank(message = "PHONE_REQUIRED")
        @Pattern(regexp = "^\\d{10}$", message = "PHONE_INVALID")
        String phoneCalled,

        @NotBlank(message = "VERIFICATION_RESULT_INVALID")
        @Pattern(
                regexp = "^(VERIFIED|UNREACHABLE|NEED_MORE_INFO)$",
                message = "VERIFICATION_RESULT_INVALID"
        )
        String result,

        @Size(max = 1000, message = "NOTE_TOO_LONG")
        String note
) {
}
