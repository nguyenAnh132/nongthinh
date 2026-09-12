package com.nongthinh.notification_service.domain.emailtemplate;

import java.time.Instant;
import java.util.Objects;

public final class EmailTemplate {

    private final Long id;
    private final Long purposeId;
    private String name;
    private String description;
    private String subject;
    private String htmlContent;
    private String textContent;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    private EmailTemplate(
            Long id,
            Long purposeId,
            String name,
            String description,
            String subject,
            String htmlContent,
            String textContent,
            boolean active,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.purposeId = Objects.requireNonNull(purposeId, "purposeId is required");
        this.name = Objects.requireNonNull(name, "name is required");
        this.description = description;
        this.subject = Objects.requireNonNull(subject, "subject is required");
        this.htmlContent = htmlContent;
        this.textContent = textContent;
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static EmailTemplate reconstruct(
            Long id,
            Long purposeId,
            String name,
            String description,
            String subject,
            String htmlContent,
            String textContent,
            boolean active,
            Instant createdAt,
            Instant updatedAt) {
        return new EmailTemplate(
                Objects.requireNonNull(id, "id is required"),
                purposeId,
                name,
                description,
                subject,
                htmlContent,
                textContent,
                active,
                createdAt,
                updatedAt);
    }

    public static EmailTemplate create(
            Long purposeId,
            String name,
            String description,
            String subject,
            String htmlContent,
            String textContent,
            Instant now) {
        return new EmailTemplate(
                null,
                purposeId,
                name,
                description,
                subject,
                htmlContent,
                textContent,
                false,
                now,
                now);
    }

    public void updateContent(
            String name,
            String description,
            String subject,
            String htmlContent,
            String textContent,
            Instant now) {
        this.name = Objects.requireNonNull(name, "name is required");
        this.description = description;
        this.subject = Objects.requireNonNull(subject, "subject is required");
        this.htmlContent = htmlContent;
        this.textContent = textContent;
        this.updatedAt = Objects.requireNonNull(now, "now is required");
    }

    public void activate(Instant now) {
        this.active = true;
        this.updatedAt = Objects.requireNonNull(now, "now is required");
    }

    public void deactivate(Instant now) {
        this.active = false;
        this.updatedAt = Objects.requireNonNull(now, "now is required");
    }

    public Long getId() {
        return id;
    }

    public Long getPurposeId() {
        return purposeId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSubject() {
        return subject;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public String getTextContent() {
        return textContent;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
