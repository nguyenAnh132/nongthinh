package com.nongthinh.notification_service.application.command.template;

public record UpdateEmailTemplateCommand(
        String name,
        String description,
        String subject,
        String htmlContent,
        String textContent
) {
}
