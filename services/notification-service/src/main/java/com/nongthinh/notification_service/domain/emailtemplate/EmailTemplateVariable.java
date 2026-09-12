package com.nongthinh.notification_service.domain.emailtemplate;

import java.time.Instant;
import java.util.Objects;

public final class EmailTemplateVariable {

    private final Long id;
    private final Long purposeId;
    private final String variableName;
    private String description;
    private String exampleValue;
    private boolean required;
    private final Instant createdAt;
    private Instant updatedAt;

    private EmailTemplateVariable(
            Long id,
            Long purposeId,
            String variableName,
            String description,
            String exampleValue,
            boolean required,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.purposeId = Objects.requireNonNull(purposeId, "purposeId is required");
        this.variableName = Objects.requireNonNull(variableName, "variableName is required");
        this.description = description;
        this.exampleValue = exampleValue;
        this.required = required;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static EmailTemplateVariable reconstruct(
            Long id,
            Long purposeId,
            String variableName,
            String description,
            String exampleValue,
            boolean required,
            Instant createdAt,
            Instant updatedAt) {
        return new EmailTemplateVariable(
                id,
                purposeId,
                variableName,
                description,
                exampleValue,
                required,
                createdAt,
                updatedAt);
    }

    public Long getId() {
        return id;
    }

    public Long getPurposeId() {
        return purposeId;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getDescription() {
        return description;
    }

    public String getExampleValue() {
        return exampleValue;
    }

    public boolean isRequired() {
        return required;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
