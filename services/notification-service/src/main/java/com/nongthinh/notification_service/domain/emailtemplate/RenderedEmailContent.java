package com.nongthinh.notification_service.domain.emailtemplate;

public record RenderedEmailContent(
        String subject,
        String htmlContent,
        String textContent
) {
}
