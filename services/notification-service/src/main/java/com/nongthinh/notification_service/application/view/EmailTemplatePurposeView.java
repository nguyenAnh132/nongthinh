package com.nongthinh.notification_service.application.view;

import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplatePurpose;

import java.time.Instant;

public record EmailTemplatePurposeView(
        Long id,
        String code,
        String name,
        String description,
        boolean systemDefined,
        Instant createdAt,
        Instant updatedAt
) {
    public static EmailTemplatePurposeView fromDomain(EmailTemplatePurpose emailTemplatePurpose){
        return new EmailTemplatePurposeView(
                emailTemplatePurpose.getId(),
                emailTemplatePurpose.getCode(),
                emailTemplatePurpose.getName(),
                emailTemplatePurpose.getDescription(),
                emailTemplatePurpose.isSystemDefined(),
                emailTemplatePurpose.getCreatedAt(),
                emailTemplatePurpose.getUpdatedAt()
        );
    }
}
