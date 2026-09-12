package com.nongthinh.profile_service.domain.branddocument;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import com.nongthinh.profile_service.domain.branddocument.valueobject.BrandDocumentReviewStatus;

public final class BrandDocument {

    private final UUID id;
    private final UUID brandProfileId;
    private String businessLicenseUrl;
    private BrandDocumentReviewStatus reviewStatus;
    private UUID reviewedBy;
    private Instant reviewedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private BrandDocument(
        UUID id,
        UUID brandProfileId,
        String businessLicenseUrl,
        BrandDocumentReviewStatus reviewStatus,
        UUID reviewedBy,
        Instant reviewedAt,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.brandProfileId = Objects.requireNonNull(brandProfileId, "brandProfileId is required");
        this.businessLicenseUrl = Objects.requireNonNull(businessLicenseUrl, "businessLicenseUrl is required");
        this.reviewStatus = Objects.requireNonNull(reviewStatus, "reviewStatus is required");
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static BrandDocument create(
        UUID id,
        UUID brandProfileId,
        String businessLicenseUrl,
        Instant now
    ) {
        return new BrandDocument(
            id,
            brandProfileId,
            businessLicenseUrl,
            BrandDocumentReviewStatus.PENDING_REVIEW,
            null,
            null,
            now,
            now
        );
    }

    public static BrandDocument reconstruct(
        UUID id,
        UUID brandProfileId,
        String businessLicenseUrl,
        BrandDocumentReviewStatus reviewStatus,
        UUID reviewedBy,
        Instant reviewedAt,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new BrandDocument(
            id,
            brandProfileId,
            businessLicenseUrl,
            reviewStatus,
            reviewedBy,
            reviewedAt,
            createdAt,
            updatedAt
        );
    }

    public void replaceBusinessLicense(String businessLicenseUrl, Instant now) {
        this.businessLicenseUrl = Objects.requireNonNull(businessLicenseUrl, "businessLicenseUrl is required");
        this.reviewStatus = BrandDocumentReviewStatus.PENDING_REVIEW;
        this.reviewedBy = null;
        this.reviewedAt = null;
        touch(now);
    }

    public void approve(UUID reviewedBy, Instant now) {
        this.reviewStatus = BrandDocumentReviewStatus.APPROVED;
        this.reviewedBy = Objects.requireNonNull(reviewedBy, "reviewedBy is required");
        this.reviewedAt = now;
        touch(now);
    }

    public void reject(UUID reviewedBy, Instant now) {
        this.reviewStatus = BrandDocumentReviewStatus.REJECTED;
        this.reviewedBy = Objects.requireNonNull(reviewedBy, "reviewedBy is required");
        this.reviewedAt = now;
        touch(now);
    }

    public void requestRevision(UUID reviewedBy, Instant now) {
        this.reviewStatus = BrandDocumentReviewStatus.NEEDS_REVISION;
        this.reviewedBy = Objects.requireNonNull(reviewedBy, "reviewedBy is required");
        this.reviewedAt = now;
        touch(now);
    }

    private void touch(Instant now) {
        this.updatedAt = Objects.requireNonNull(now, "now is required");
    }

    public UUID getId() {
        return id;
    }

    public UUID getBrandProfileId() {
        return brandProfileId;
    }

    public String getBusinessLicenseUrl() {
        return businessLicenseUrl;
    }

    public BrandDocumentReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
