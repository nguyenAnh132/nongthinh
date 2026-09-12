package com.nongthinh.agri_catalog_service.domain.product;

import java.time.Instant;
import java.util.UUID;

import com.nongthinh.agri_catalog_service.domain.product.valueobject.ProductReviewStatus;

public class ProductReview {

    private final UUID id;
    private final UUID productId;
    private final UUID farmerId;

    private short rating;
    private String comment;

    private ProductReviewStatus status;
    private String moderationReason;
    private Instant hiddenAt;
    private UUID hiddenBy;

    private final Instant createdAt;
    private final UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;
    private Instant deletedAt;
    private UUID deletedBy;

    private long version;

    private ProductReview(
        UUID id,
        UUID productId,
        UUID farmerId,
        short rating,
        String comment,
        ProductReviewStatus status,
        String moderationReason,
        Instant hiddenAt,
        UUID hiddenBy,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Instant deletedAt,
        UUID deletedBy,
        long version
    ) {
        this.id = id;
        this.productId = productId;
        this.farmerId = farmerId;
        this.rating = rating;
        this.comment = comment;
        this.status = status;
        this.moderationReason = moderationReason;
        this.hiddenAt = hiddenAt;
        this.hiddenBy = hiddenBy;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
        this.version = version;
    }

    public static ProductReview create(
        UUID id,
        UUID productId,
        UUID farmerId,
        short rating,
        String comment,
        UUID createdBy,
        Instant now
    ) {
        validateRequiredFields(
            id,
            productId,
            farmerId,
            createdBy,
            now
        );

        validateRating(rating);

        if (!farmerId.equals(createdBy)) {
            throw new IllegalArgumentException(
                "Created by must match farmer ID"
            );
        }

        return new ProductReview(
            id,
            productId,
            farmerId,
            rating,
            comment,
            ProductReviewStatus.VISIBLE,
            null,
            null,
            null,
            now,
            createdBy,
            now,
            createdBy,
            null,
            null,
            0L
        );
    }

    public static ProductReview reconstruct(
        UUID id,
        UUID productId,
        UUID farmerId,
        short rating,
        String comment,
        ProductReviewStatus status,
        String moderationReason,
        Instant hiddenAt,
        UUID hiddenBy,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Instant deletedAt,
        UUID deletedBy,
        long version
    ) {
        return new ProductReview(
            id,
            productId,
            farmerId,
            rating,
            comment,
            status,
            moderationReason,
            hiddenAt,
            hiddenBy,
            createdAt,
            createdBy,
            updatedAt,
            updatedBy,
            deletedAt,
            deletedBy,
            version
        );
    }

    public void update(
        short rating,
        String comment,
        UUID updatedBy,
        Instant now
    ) {
        validateRating(rating);
        validateOwner(updatedBy);
        ensureNotDeleted();

        this.rating = rating;
        this.comment = comment;

        this.touch(now, updatedBy);
    }

    public void changeRating(
        short rating,
        UUID updatedBy,
        Instant now
    ) {
        validateRating(rating);
        validateOwner(updatedBy);
        ensureNotDeleted();

        this.rating = rating;
        this.touch(now, updatedBy);
    }

    public void changeComment(
        String comment,
        UUID updatedBy,
        Instant now
    ) {
        validateOwner(updatedBy);
        ensureNotDeleted();

        this.comment = comment;
        this.touch(now, updatedBy);
    }

    public void hide(
        String moderationReason,
        UUID adminId,
        Instant now
    ) {
        ensureNotDeleted();

        if (status == ProductReviewStatus.HIDDEN) {
            throw new IllegalStateException(
                "Product review is already hidden"
            );
        }

        if (moderationReason == null || moderationReason.isBlank()) {
            throw new IllegalArgumentException(
                "Moderation reason is required when hiding a review"
            );
        }

        this.status = ProductReviewStatus.HIDDEN;
        this.moderationReason = moderationReason;
        this.hiddenAt = now;
        this.hiddenBy = adminId;

        this.touch(now, adminId);
    }

    public void show(
        UUID adminId,
        Instant now
    ) {
        ensureNotDeleted();

        if (status == ProductReviewStatus.VISIBLE) {
            throw new IllegalStateException(
                "Product review is already visible"
            );
        }

        this.status = ProductReviewStatus.VISIBLE;
        this.moderationReason = null;
        this.hiddenAt = null;
        this.hiddenBy = null;

        this.touch(now, adminId);
    }

    public void delete(
        UUID deletedBy,
        Instant now
    ) {
        if (isDeleted()) {
            throw new IllegalStateException(
                "Product review is already deleted"
            );
        }

        this.deletedAt = now;
        this.deletedBy = deletedBy;

        this.touch(now, deletedBy);
    }

    public void restore(
        UUID restoredBy,
        Instant now
    ) {
        if (!isDeleted()) {
            throw new IllegalStateException(
                "Product review is not deleted"
            );
        }

        this.deletedAt = null;
        this.deletedBy = null;

        this.touch(now, restoredBy);
    }

    private void touch(
        Instant now,
        UUID updatedBy
    ) {
        if (now == null) {
            throw new IllegalArgumentException(
                "Updated time must not be null"
            );
        }

        if (updatedBy == null) {
            throw new IllegalArgumentException(
                "Updated by must not be null"
            );
        }

        this.updatedAt = now;
        this.updatedBy = updatedBy;
    }

    private void validateOwner(UUID actorId) {
        if (actorId == null) {
            throw new IllegalArgumentException(
                "Actor ID must not be null"
            );
        }

        if (!farmerId.equals(actorId)) {
            throw new IllegalStateException(
                "Only the review owner can update this review"
            );
        }
    }

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new IllegalStateException(
                "Deleted product review cannot be modified"
            );
        }
    }

    private static void validateRating(short rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                "Rating must be between 1 and 5"
            );
        }
    }

    private static void validateRequiredFields(
        UUID id,
        UUID productId,
        UUID farmerId,
        UUID createdBy,
        Instant createdAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException(
                "Product review ID must not be null"
            );
        }

        if (productId == null) {
            throw new IllegalArgumentException(
                "Product ID must not be null"
            );
        }

        if (farmerId == null) {
            throw new IllegalArgumentException(
                "Farmer ID must not be null"
            );
        }

        if (createdBy == null) {
            throw new IllegalArgumentException(
                "Created by must not be null"
            );
        }

        if (createdAt == null) {
            throw new IllegalArgumentException(
                "Created time must not be null"
            );
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isVisible() {
        return status == ProductReviewStatus.VISIBLE && !isDeleted();
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getFarmerId() {
        return farmerId;
    }

    public short getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public ProductReviewStatus getStatus() {
        return status;
    }

    public String getModerationReason() {
        return moderationReason;
    }

    public Instant getHiddenAt() {
        return hiddenAt;
    }

    public UUID getHiddenBy() {
        return hiddenBy;
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

    public long getVersion() {
        return version;
    }
}