package com.nongthinh.auth_service.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailOtpRequest(
        @Email(message = "EMAIL_REQUEST_FORMAT_INVALID")
        String email,
        @NotBlank(message = "OTP_REQUIRED")
        @Pattern(regexp = "^\\d{4,8}$", message = "OTP_INVALID")
        String otp
) {
}
