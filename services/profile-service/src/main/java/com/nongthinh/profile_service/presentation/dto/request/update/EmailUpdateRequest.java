package com.nongthinh.profile_service.presentation.dto.request.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailUpdateRequest(

    @NotBlank(message = "REPRESENTATIVE_EMAIL_REQUIRED")
    @Email(message = "REPRESENTATIVE_EMAIL_INVALID")
    String email
) {
}
