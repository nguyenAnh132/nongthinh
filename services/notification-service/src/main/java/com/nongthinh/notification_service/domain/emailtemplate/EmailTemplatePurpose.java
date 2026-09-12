package com.nongthinh.notification_service.domain.emailtemplate;

import java.time.Instant;
import java.util.Objects;

public final class EmailTemplatePurpose {

    private final Long id;
    private final String code;
    private String name;
    private String description;
    private final boolean systemDefined;
    private final Instant createdAt;
    private Instant updatedAt;

    private EmailTemplatePurpose(
            Long id,
            String code,
            String name,
            String description,
            boolean systemDefined,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.code = Objects.requireNonNull(code, "code is required");
        this.name = Objects.requireNonNull(name, "name is required");
        this.description = description;
        this.systemDefined = systemDefined;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static EmailTemplatePurpose reconstruct(
            Long id,
            String code,
            String name,
            String description,
            boolean systemDefined,
            Instant createdAt,
            Instant updatedAt) {
        return new EmailTemplatePurpose(id, code, name, description, systemDefined, createdAt, updatedAt);
    }

    public void update(String name, String description, Instant now) {
        this.name = Objects.requireNonNull(name, "name is required");
        this.description = description;
        this.updatedAt = Objects.requireNonNull(now, "now is required");
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSystemDefined() {
        return systemDefined;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
