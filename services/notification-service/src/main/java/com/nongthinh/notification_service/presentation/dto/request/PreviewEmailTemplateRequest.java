package com.nongthinh.notification_service.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PreviewEmailTemplateRequest(
        @NotBlank(message = "TEMPLATE_SUBJECT_REQUIRED")
        String subject,
        String htmlContent,
        String textContent
) {
}
