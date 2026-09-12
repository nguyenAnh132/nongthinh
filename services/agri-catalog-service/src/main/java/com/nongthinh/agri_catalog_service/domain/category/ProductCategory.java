package com.nongthinh.agri_catalog_service.domain.category;

import java.time.Instant;
import java.util.UUID;

public class ProductCategory {

    private final UUID id;
    private UUID parentId;
    private String name;
    private String slug;
    private String description;
    private int displayOrder;
    private boolean isActive;
    private final Instant createdAt;
    private UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;
    private Instant deletedAt;
    private UUID deletedBy;

    private ProductCategory(
        UUID id,
        UUID parentId,
        String name,
        String slug,
        String description,
        int displayOrder,
        boolean isActive,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Instant deletedAt,
        UUID deletedBy
    ) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public static ProductCategory create(
        UUID id,
        UUID parentId,
        String name,
        String slug,
        String description,
        int displayOrder,
        UUID createdBy,
        UUID updatedBy,
        UUID deletedBy,
        Instant now
    ) {
        return new ProductCategory(
            id,
            parentId,
            name,
            slug,
            description,
            displayOrder,
            true,
            now,
            createdBy,
            now,
            updatedBy,
            null,
            null
        );
    }

    public static ProductCategory reconstruct(
        UUID id,
        UUID parentId,
        String name,
        String slug,
        String description,
        int displayOrder,
        boolean isActive,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Instant deletedAt,
        UUID deletedBy
    ) {
        return new ProductCategory(id, parentId, name, slug, description, displayOrder, isActive, createdAt, createdBy, updatedAt, updatedBy, deletedAt, deletedBy);
    }

    public void update(
        UUID parentId,
        String name,
        String slug,
        String description,
        int displayOrder,
        boolean isActive,
        UUID updatedBy,
        Instant now
    ) {
        this.parentId = parentId;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
        this.touch(now, updatedBy);
    }

    public void changeParent(UUID parentId) {
        this.parentId = parentId;
        this.touch(Instant.now(), this.updatedBy);
    }

    public void changeName(String name) {
        this.name = name;
        this.touch(Instant.now(), this.updatedBy);
    }

    public void changeSlug(String slug) {
        this.slug = slug;
        this.touch(Instant.now(), this.updatedBy);
    }

    public void changeDescription(String description) {
        this.description = description;
        this.touch(Instant.now(), this.updatedBy);
    }

    public void changeDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
        this.touch(Instant.now(), this.updatedBy);
    }

    public void changeIsActive(boolean isActive) {
        this.isActive = isActive;
        this.touch(Instant.now(), this.updatedBy);
    }

    public void delete() {
        this.deletedAt = Instant.now();
        this.deletedBy = this.updatedBy;
        this.touch(Instant.now(), this.updatedBy);
    }

    public void delete(UUID deletedBy, Instant now) {
        this.deletedAt = now;
        this.deletedBy = deletedBy;
        this.touch(now, deletedBy);
    }

    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
        this.touch(Instant.now(), this.updatedBy);
    }

    private void touch(Instant now, UUID updatedBy) {
        this.updatedAt = now;
        this.updatedBy = updatedBy;
    }

    public UUID getId() {
        return id;
    }

    public UUID getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isActive() {
        return isActive;
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
