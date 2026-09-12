package com.nongthinh.auth_service.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendEmailOtpRequest(
        @NotBlank(message = "EMAIL_REQUEST_FORMAT_INVALID")
        @Email(message = "EMAIL_REQUEST_FORMAT_INVALID")
        String email
) {
}
