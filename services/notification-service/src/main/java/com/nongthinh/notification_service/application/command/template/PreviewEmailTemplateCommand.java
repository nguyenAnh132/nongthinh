package com.nongthinh.notification_service.application.command.template;

public record PreviewEmailTemplateCommand(
        String subject,
        String htmlContent,
        String textContent
) {
}
