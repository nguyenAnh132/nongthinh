package com.nongthinh.agri_catalog_service.domain.disease;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;

public class Disease {

    private final UUID id;
    private final CreatedSource createdSource;
    private final UUID brandId;

    private String name;
    private String slug;
    private String scientificName;

    private UUID cropTypeId;
    private String affectedPart;
    private String pathogenType;

    private String shortDescription;
    private String description;
    private String symptoms;
    private String causes;
    private String favorableConditions;
    private String preventionMethod;
    private String treatmentGuideline;

    private String thumbnailUrl;

    private ReviewStatus reviewStatus;
    private String rejectionReason;

    private Instant submittedAt;
    private Instant reviewedAt;
    private UUID reviewedBy;
    private Instant publishedAt;

    private final Instant createdAt;
    private final UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;
    private Instant deletedAt;
    private UUID deletedBy;

    private Disease(
        UUID id,
        CreatedSource createdSource,
        UUID brandId,
        String name,
        String slug,
        String scientificName,
        UUID cropTypeId,
        String affectedPart,
        String pathogenType,
        String shortDescription,
        String description,
        String symptoms,
        String causes,
        String favorableConditions,
        String preventionMethod,
        String treatmentGuideline,
        String thumbnailUrl,
        ReviewStatus reviewStatus,
        String rejectionReason,
        Instant submittedAt,
        Instant reviewedAt,
        UUID reviewedBy,
        Instant publishedAt,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Instant deletedAt,
        UUID deletedBy
    ) {
        this.id = id;
        this.createdSource = createdSource;
        this.brandId = brandId;
        this.name = name;
        this.slug = slug;
        this.scientificName = scientificName;
        this.cropTypeId = cropTypeId;
        this.affectedPart = affectedPart;
        this.pathogenType = pathogenType;
        this.shortDescription = shortDescription;
        this.description = description;
        this.symptoms = symptoms;
        this.causes = causes;
        this.favorableConditions = favorableConditions;
        this.preventionMethod = preventionMethod;
        this.treatmentGuideline = treatmentGuideline;
        this.thumbnailUrl = thumbnailUrl;
        this.reviewStatus = reviewStatus;
        this.rejectionReason = rejectionReason;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
        this.reviewedBy = reviewedBy;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public static Disease create(
        UUID id,
        CreatedSource createdSource,
        UUID brandId,
        String name,
        String slug,
        String scientificName,
        UUID cropTypeId,
        String affectedPart,
        String pathogenType,
        String shortDescription,
        String description,
        String symptoms,
        String causes,
        String favorableConditions,
        String preventionMethod,
        String treatmentGuideline,
        String thumbnailUrl,
        UUID createdBy,
        Instant now
    ) {
        validateSource(createdSource, brandId);

        ReviewStatus initialStatus = createdSource == CreatedSource.ADMIN
            ? ReviewStatus.APPROVED
            : ReviewStatus.DRAFT;

        Instant publishedAt = createdSource == CreatedSource.ADMIN
            ? now
            : null;

        return new Disease(
            id,
            createdSource,
            brandId,
            name,
            slug,
            scientificName,
            cropTypeId,
            affectedPart,
            pathogenType,
            shortDescription,
            description,
            symptoms,
            causes,
            favorableConditions,
            preventionMethod,
            treatmentGuideline,
            thumbnailUrl,
            initialStatus,
            null,
            null,
            null,
            null,
            publishedAt,
            now,
            createdBy,
            now,
            createdBy,
            null,
            null
        );
    }

    public static Disease reconstruct(
        UUID id,
        CreatedSource createdSource,
        UUID brandId,
        String name,
        String slug,
        String scientificName,
        UUID cropTypeId,
        String affectedPart,
        String pathogenType,
        String shortDescription,
        String description,
        String symptoms,
        String causes,
        String favorableConditions,
        String preventionMethod,
        String treatmentGuideline,
        String thumbnailUrl,
        ReviewStatus reviewStatus,
        String rejectionReason,
        Instant submittedAt,
        Instant reviewedAt,
        UUID reviewedBy,
        Instant publishedAt,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Instant deletedAt,
        UUID deletedBy
    ) {
        return new Disease(
            id,
            createdSource,
            brandId,
            name,
            slug,
            scientificName,
            cropTypeId,
            affectedPart,
            pathogenType,
            shortDescription,
            description,
            symptoms,
            causes,
            favorableConditions,
            preventionMethod,
            treatmentGuideline,
            thumbnailUrl,
            reviewStatus,
            rejectionReason,
            submittedAt,
            reviewedAt,
            reviewedBy,
            publishedAt,
            createdAt,
            createdBy,
            updatedAt,
            updatedBy,
            deletedAt,
            deletedBy
        );
    }

    public void update(
        String name,
        String slug,
        String scientificName,
        UUID cropTypeId,
        String affectedPart,
        String pathogenType,
        String shortDescription,
        String description,
        String symptoms,
        String causes,
        String favorableConditions,
        String preventionMethod,
        String treatmentGuideline,
        String thumbnailUrl,
        UUID updatedBy,
        Instant now
    ) {
        this.name = name;
        this.slug = slug;
        this.scientificName = scientificName;
        this.cropTypeId = cropTypeId;
        this.affectedPart = affectedPart;
        this.pathogenType = pathogenType;
        this.shortDescription = shortDescription;
        this.description = description;
        this.symptoms = symptoms;
        this.causes = causes;
        this.favorableConditions = favorableConditions;
        this.preventionMethod = preventionMethod;
        this.treatmentGuideline = treatmentGuideline;
        this.thumbnailUrl = thumbnailUrl;

        this.touch(now, updatedBy);
    }

    public void submitForReview(
        UUID updatedBy,
        Instant now
    ) {
        if (reviewStatus != ReviewStatus.DRAFT && reviewStatus != ReviewStatus.REJECTED) {
            throw new IllegalStateException("Only draft or rejected disease can be submitted");
        }
        this.reviewStatus = ReviewStatus.PENDING_REVIEW;
        this.rejectionReason = null;
        this.submittedAt = now;
        this.reviewedAt = null;
        this.reviewedBy = null;
        this.touch(now, updatedBy);
    }

    public void approve(
        UUID reviewedBy,
        Instant now
    ) {
        requirePendingReview();
        this.reviewStatus = ReviewStatus.APPROVED;
        this.rejectionReason = null;
        this.reviewedAt = now;
        this.reviewedBy = reviewedBy;
        this.publishedAt = now;
        this.touch(now, reviewedBy);
    }

    public void reject(
        String rejectionReason,
        UUID reviewedBy,
        Instant now
    ) {
        requirePendingReview();
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason must not be blank");
        }
        this.reviewStatus = ReviewStatus.REJECTED;
        this.rejectionReason = rejectionReason;
        this.reviewedAt = now;
        this.reviewedBy = reviewedBy;
        this.publishedAt = null;
        this.touch(now, reviewedBy);
    }

    public void hide(
        UUID updatedBy,
        Instant now
    ) {
        if (reviewStatus != ReviewStatus.APPROVED) {
            throw new IllegalStateException("Only approved disease can be hidden");
        }
        this.reviewStatus = ReviewStatus.HIDDEN;
        this.publishedAt = null;
        this.touch(now, updatedBy);
    }

    public void restorePublication(
        UUID updatedBy,
        Instant now
    ) {
        if (reviewStatus != ReviewStatus.HIDDEN) {
            throw new IllegalStateException("Only hidden disease can be restored");
        }
        this.reviewStatus = ReviewStatus.APPROVED;
        this.publishedAt = now;
        this.touch(now, updatedBy);
    }

    public void returnToDraft(
        UUID updatedBy,
        Instant now
    ) {
        this.reviewStatus = ReviewStatus.DRAFT;
        this.rejectionReason = null;
        this.submittedAt = null;
        this.reviewedAt = null;
        this.reviewedBy = null;
        this.publishedAt = null;
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

    private void requirePendingReview() {
        if (reviewStatus != ReviewStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Disease must be pending review");
        }
    }

    private static void validateSource(
        CreatedSource createdSource,
        UUID brandId
    ) {
        if (createdSource == null) {
            throw new IllegalArgumentException(
                "Created source must not be null"
            );
        }

        if (createdSource == CreatedSource.ADMIN && brandId != null) {
            throw new IllegalArgumentException(
                "Brand ID must be null when disease is created by Admin"
            );
        }

        if (createdSource == CreatedSource.BRAND && brandId == null) {
            throw new IllegalArgumentException(
                "Brand ID is required when disease is created by Brand"
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public CreatedSource getCreatedSource() {
        return createdSource;
    }

    public UUID getBrandId() {
        return brandId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getScientificName() {
        return scientificName;
    }

    public UUID getCropTypeId() {
        return cropTypeId;
    }

    public String getAffectedPart() {
        return affectedPart;
    }

    public String getPathogenType() {
        return pathogenType;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public String getCauses() {
        return causes;
    }

    public String getFavorableConditions() {
        return favorableConditions;
    }

    public String getPreventionMethod() {
        return preventionMethod;
    }

    public String getTreatmentGuideline() {
        return treatmentGuideline;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public Instant getPublishedAt() {
        return publishedAt;
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
