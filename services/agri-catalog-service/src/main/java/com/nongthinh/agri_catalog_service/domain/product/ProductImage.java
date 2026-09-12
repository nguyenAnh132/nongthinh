package com.nongthinh.agri_catalog_service.domain.product;

import java.time.Instant;
import java.util.UUID;

public class ProductImage {

    private final UUID id;
    private final UUID productId;

    private UUID fileId;
    private String imageUrl;
    private String altText;
    private int displayOrder;
    private boolean isPrimary;

    private final Instant createdAt;
    private final UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;
    private Instant deletedAt;
    private UUID deletedBy;

    private ProductImage(
        UUID id,
        UUID productId,
        UUID fileId,
        String imageUrl,
        String altText,
        int displayOrder,
        boolean isPrimary,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Instant deletedAt,
        UUID deletedBy
    ) {
        this.id = id;
        this.productId = productId;
        this.fileId = fileId;
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.displayOrder = displayOrder;
        this.isPrimary = isPrimary;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public static ProductImage create(
        UUID id,
        UUID productId,
        UUID fileId,
        String imageUrl,
        String altText,
        int displayOrder,
        boolean isPrimary,
        UUID createdBy,
        Instant now
    ) {
        return new ProductImage(
            id,
            productId,
            fileId,
            imageUrl,
            altText,
            displayOrder,
            isPrimary,
            now,
            createdBy,
            now,
            createdBy,
            null,
            null
        );
    }

    public static ProductImage reconstruct(
        UUID id,
        UUID productId,
        UUID fileId,
        String imageUrl,
        String altText,
        int displayOrder,
        boolean isPrimary,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Instant deletedAt,
        UUID deletedBy
    ) {
        return new ProductImage(
            id,
            productId,
            fileId,
            imageUrl,
            altText,
            displayOrder,
            isPrimary,
            createdAt,
            createdBy,
            updatedAt,
            updatedBy,
            deletedAt,
            deletedBy
        );
    }

    public void update(
        UUID fileId,
        String imageUrl,
        String altText,
        int displayOrder,
        UUID updatedBy,
        Instant now
    ) {
        this.fileId = fileId;
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.displayOrder = displayOrder;

        this.touch(now, updatedBy);
    }

    public void changeFile(
        UUID fileId,
        String imageUrl,
        UUID updatedBy,
        Instant now
    ) {
        this.fileId = fileId;
        this.imageUrl = imageUrl;

        this.touch(now, updatedBy);
    }

    public void changeImageUrl(
        String imageUrl,
        UUID updatedBy,
        Instant now
    ) {
        this.imageUrl = imageUrl;
        this.touch(now, updatedBy);
    }

    public void changeAltText(
        String altText,
        UUID updatedBy,
        Instant now
    ) {
        this.altText = altText;
        this.touch(now, updatedBy);
    }

    public void changeDisplayOrder(
        int displayOrder,
        UUID updatedBy,
        Instant now
    ) {
        this.displayOrder = displayOrder;
        this.touch(now, updatedBy);
    }

    public void markAsPrimary(
        UUID updatedBy,
        Instant now
    ) {
        this.isPrimary = true;
        this.touch(now, updatedBy);
    }

    public void removePrimary(
        UUID updatedBy,
        Instant now
    ) {
        this.isPrimary = false;
        this.touch(now, updatedBy);
    }

    public void delete(
        UUID deletedBy,
        Instant now
    ) {
        this.deletedAt = now;
        this.deletedBy = deletedBy;

        this.touch(now, deletedBy);
    }

    public void restore(
        UUID restoredBy,
        Instant now
    ) {
        this.deletedAt = null;
        this.deletedBy = null;

        this.touch(now, restoredBy);
    }

    private void touch(
        Instant now,
        UUID updatedBy
    ) {
        this.updatedAt = now;
        this.updatedBy = updatedBy;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getFileId() {
        return fileId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAltText() {
        return altText;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isPrimary() {
        return isPrimary;
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

    public boolean isDeleted() {
        return deletedAt != null;
    }
}