package com.nongthinh.bo_portal_service.domain.systemparam;

import com.nongthinh.bo_portal_service.common.exception.ErrorCode;
import com.nongthinh.bo_portal_service.domain.exception.BusinessException;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

public final class SystemParam {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{1,99}$");

    private final Long id;
    private final String name;
    private String value;
    private String description;
    private final SystemParamDataType dataType;
    private final boolean systemDefined;
    private Long typeId;
    private final Instant createdAt;
    private Instant updatedAt;

    private SystemParam(
            Long id,
            String name,
            String value,
            String description,
            SystemParamDataType dataType,
            boolean systemDefined,
            Long typeId,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name is required");
        this.value = Objects.requireNonNull(value, "value is required");
        this.description = description;
        this.dataType = Objects.requireNonNull(dataType, "dataType is required");
        this.systemDefined = systemDefined;
        this.typeId = Objects.requireNonNull(typeId, "typeId is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static SystemParam create(
            String name,
            String value,
            String description,
            SystemParamDataType dataType,
            Long typeId,
            Instant now) {
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(dataType, "dataType is required");
        Objects.requireNonNull(now, "now is required");
        if (typeId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_TYPE_ID_REQUIRED);
        }

        String normalizedName = name.trim().toUpperCase();
        if (!NAME_PATTERN.matcher(normalizedName).matches()) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_PARAM_NAME_REQUIRED,
                    "Name must be UPPER_SNAKE_CASE and 2-100 chars: " + name);
        }
        dataType.validate(value);

        return new SystemParam(null, normalizedName, value.trim(), description, dataType, false, typeId, now, now);
    }

    public static SystemParam reconstruct(
            Long id,
            String name,
            String value,
            String description,
            SystemParamDataType dataType,
            boolean systemDefined,
            Long typeId,
            Instant createdAt,
            Instant updatedAt) {
        return new SystemParam(id, name, value, description, dataType, systemDefined, typeId, createdAt, updatedAt);
    }

    public void update(String value, String description, Instant now) {
        Objects.requireNonNull(now, "now is required");
        dataType.validate(value);
        this.value = value.trim();
        this.description = description;
        this.updatedAt = now;
    }

    public void changeType(Long typeId, Instant now) {
        Objects.requireNonNull(now, "now is required");
        if (typeId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_TYPE_ID_REQUIRED);
        }
        this.typeId = typeId;
        this.updatedAt = now;
    }

    public void ensureDeletable() {
        if (systemDefined) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_CANNOT_DELETE, name);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public SystemParamDataType getDataType() {
        return dataType;
    }

    public boolean isSystemDefined() {
        return systemDefined;
    }

    public Long getTypeId() {
        return typeId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
