package com.nongthinh.notification_service.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateEmailTemplateRequest(
        @NotBlank(message = "TEMPLATE_NAME_REQUIRED")
        String name,
        String description,
        @NotBlank(message = "TEMPLATE_SUBJECT_REQUIRED")
        String subject,
        String htmlContent,
        String textContent
) {
}
