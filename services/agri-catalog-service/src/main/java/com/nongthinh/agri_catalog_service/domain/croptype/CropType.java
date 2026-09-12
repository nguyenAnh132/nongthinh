package com.nongthinh.agri_catalog_service.domain.croptype;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class CropType {

    private final UUID id;
    private final String code;
    private String name;
    private String description;
    private boolean active;
    private final Instant createdAt;
    private final UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;
    private Instant deletedAt;
    private UUID deletedBy;

    private CropType(
            UUID id,
            String code,
            String name,
            String description,
            boolean active,
            Instant createdAt,
            UUID createdBy,
            Instant updatedAt,
            UUID updatedBy,
            Instant deletedAt,
            UUID deletedBy) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public static CropType create(
            UUID id,
            String code,
            String name,
            String description,
            UUID createdBy,
            Instant now) {
        return new CropType(
                id,
                normalizeCode(code),
                name,
                description,
                true,
                now,
                createdBy,
                now,
                createdBy,
                null,
                null);
    }

    public static CropType reconstruct(
            UUID id,
            String code,
            String name,
            String description,
            boolean active,
            Instant createdAt,
            UUID createdBy,
            Instant updatedAt,
            UUID updatedBy,
            Instant deletedAt,
            UUID deletedBy) {
        return new CropType(
                id,
                normalizeCode(code),
                name,
                description,
                active,
                createdAt,
                createdBy,
                updatedAt,
                updatedBy,
                deletedAt,
                deletedBy);
    }

    public void update(
            String name,
            String description,
            boolean active,
            UUID updatedBy,
            Instant now) {
        this.name = name;
        this.description = description;
        this.active = active;
        touch(now, updatedBy);
    }

    public void delete(UUID deletedBy, Instant now) {
        this.deletedAt = now;
        this.deletedBy = deletedBy;
        touch(now, deletedBy);
    }

    private static String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }

    private void touch(Instant now, UUID updatedBy) {
        this.updatedAt = now;
        this.updatedBy = updatedBy;
    }

    public UUID getId() {
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

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public UUID getDeletedBy() {
        return deletedBy;
    }
}

