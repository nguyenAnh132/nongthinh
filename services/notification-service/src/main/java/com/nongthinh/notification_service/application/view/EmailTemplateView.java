package com.nongthinh.notification_service.application.view;

import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplate;

import java.time.Instant;

public record EmailTemplateView(
        Long id,
        Long purposeId,
        String name,
        String description,
        String subject,
        String htmlContent,
        String textContent,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static EmailTemplateView fromDomain(EmailTemplate emailTemplate) {
        return new EmailTemplateView(
                emailTemplate.getId(),
                emailTemplate.getPurposeId(),
                emailTemplate.getName(),
                emailTemplate.getDescription(),
                emailTemplate.getSubject(),
                emailTemplate.getHtmlContent(),
                emailTemplate.getTextContent(),
                emailTemplate.isActive(),
                emailTemplate.getCreatedAt(),
                emailTemplate.getUpdatedAt()
        );
    }
}
