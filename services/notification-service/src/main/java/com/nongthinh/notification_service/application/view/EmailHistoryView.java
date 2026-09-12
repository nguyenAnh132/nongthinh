package com.nongthinh.notification_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.notification_service.domain.emailhistory.EmailHistory;

public record EmailHistoryView(
    Long id,
    UUID userId,
    Long templateId,
    Long purposeId,
    String status,
    Instant sendAt
) {

    public static EmailHistoryView fromDomain(EmailHistory emailHistory) {

        return new EmailHistoryView(
            emailHistory.getId(),
            emailHistory.getUserId(),
            emailHistory.getTemplateId(),
            emailHistory.getPurposeId(),
            emailHistory.getStatus(),
            emailHistory.getSendAt()
        );
    }
}
