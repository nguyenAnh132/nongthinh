package com.nongthinh.bo_portal_service.domain.systemparam;

import com.nongthinh.bo_portal_service.common.exception.ErrorCode;
import com.nongthinh.bo_portal_service.domain.exception.BusinessException;
import java.time.Instant;
import java.util.Objects;

public final class SystemParamType {

    private static final int NAME_MAX_LENGTH = 100;

    private final Long id;
    private String name;
    private String description;
    private final boolean systemDefined;
    private final Instant createdAt;
    private Instant updatedAt;

    private SystemParamType(
            Long id,
            String name,
            String description,
            boolean systemDefined,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name is required");
        this.description = description;
        this.systemDefined = systemDefined;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static SystemParamType create(String name, String description, Instant now) {
        Objects.requireNonNull(now, "now is required");
        String normalizedName = normalizeName(name);
        return new SystemParamType(null, normalizedName, description, false, now, now);
    }

    public static SystemParamType reconstruct(
            Long id,
            String name,
            String description,
            boolean systemDefined,
            Instant createdAt,
            Instant updatedAt) {
        return new SystemParamType(id, name, description, systemDefined, createdAt, updatedAt);
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_TYPE_NAME_REQUIRED);
        }
        String trimmed = name.trim();
        if (trimmed.length() > NAME_MAX_LENGTH) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_PARAM_TYPE_NAME_REQUIRED,
                    "Name must not exceed " + NAME_MAX_LENGTH + " characters");
        }
        return trimmed;
    }

    public Long getId() {
        return id;
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
