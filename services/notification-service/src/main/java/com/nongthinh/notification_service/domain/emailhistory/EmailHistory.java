package com.nongthinh.notification_service.domain.emailhistory;

import java.time.Instant;
import java.util.UUID;

public class EmailHistory {

    private Long id;
    private UUID userId;
    private Long templateId;
    private Long purposeId;
    private String status;
    private Instant sendAt;

    private EmailHistory (
        Long id,
        UUID userId,
        Long templateId,
        Long purposeId,
        String status,
        Instant sendAt
    ) {
        this.id = id;
        this.userId = userId;
        this.templateId = templateId;
        this.purposeId = purposeId;
        this.status = status;
        this.sendAt = sendAt;
    }

    private EmailHistory(
        UUID userId,
        Long templateId,
        Long purposeId,
        String status,
        Instant sendAt
    ) {
        this.userId = userId;
        this.templateId = templateId;
        this.purposeId = purposeId;
        this.status = status;
        this.sendAt = sendAt;
    }

    public static EmailHistory create(
        UUID userId,
        Long templateId,
        Long purposeId,
        String status,
        Instant sendAt
    ) {
        return new EmailHistory(userId, templateId, purposeId, status, sendAt);
    }

    public static EmailHistory reconstruct(
        Long id,
        UUID userId,
        Long templateId,
        Long purposeId,
        String status,
        Instant sendAt
    ) {
        return new EmailHistory(id, userId, templateId, purposeId, status, sendAt);
    }

    public Long getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public Long getPurposeId() {
        return purposeId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getSendAt() {
        return sendAt;
    }
    
}
