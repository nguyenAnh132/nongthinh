package com.nongthinh.profile_service.domain.brandprofile;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandName;
import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandProfileStatus;
import com.nongthinh.profile_service.domain.shared.valueobject.Address;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;

public final class BrandProfile {

    private static final Duration REJECTION_GRACE_PERIOD = Duration.ofDays(5);

    private final UUID id;

    private final UUID userId;

    private BrandName brandName;

    private String taxCode;

    private String description;

    private String phone;

    private Address officeAddress;

    private String representativeName;

    private String representativePhone;

    private String representativeEmail;

    private String logoUrl;

    private String bannerUrl;

    private String websiteUrl;

    private BrandProfileStatus status;

    private String rejectionReason;

    private Instant scheduledDeletionAt;

    private Instant rejectedAt;

    private Instant approvedAt;

    private UUID approvedBy;

    private UUID rejectedBy;

    private final Instant createdAt;

    private Instant updatedAt;

    private BrandProfile(
        UUID id,
        UUID userId,
        BrandName brandName,
        String taxCode,
        String description,
        String phone,
        Address officeAddress,
        String representativeName,
        String representativePhone,
        String representativeEmail,
        String logoUrl,
        String bannerUrl,
        String websiteUrl,
        BrandProfileStatus status,
        String rejectionReason,
        Instant scheduledDeletionAt,
        Instant rejectedAt,
        Instant approvedAt,
        UUID approvedBy,
        UUID rejectedBy,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.brandName = Objects.requireNonNull(brandName, "brandName is required");
        this.taxCode = taxCode;
        this.description = description;
        this.phone = Objects.requireNonNull(phone, "phone is required");
        this.officeAddress = officeAddress != null ? officeAddress : Address.empty();
        this.representativeName = Objects.requireNonNull(representativeName, "representativeName is required");
        this.representativePhone = Objects.requireNonNull(representativePhone, "representativePhone is required");
        this.representativeEmail = Objects.requireNonNull(representativeEmail, "representativeEmail is required");
        this.logoUrl = logoUrl;
        this.bannerUrl = bannerUrl;
        this.websiteUrl = websiteUrl;
        this.status = Objects.requireNonNull(status, "status is required");
        this.rejectionReason = rejectionReason;
        this.scheduledDeletionAt = scheduledDeletionAt;
        this.rejectedAt = rejectedAt;
        this.approvedAt = approvedAt;
        this.approvedBy = approvedBy;
        this.rejectedBy = rejectedBy;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static BrandProfile reconstruct(
        UUID id,
        UUID userId,
        BrandName brandName,
        String taxCode,
        String description,
        String phone,
        Address officeAddress,
        String representativeName,
        String representativePhone,
        String representativeEmail,
        String logoUrl,
        String bannerUrl,
        String websiteUrl,
        BrandProfileStatus status,
        String rejectionReason,
        Instant scheduledDeletionAt,
        Instant rejectedAt,
        Instant approvedAt,
        UUID approvedBy,
        UUID rejectedBy,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new BrandProfile(
            id,
            userId,
            brandName,
            taxCode,
            description,
            phone,
            officeAddress,
            representativeName,
            representativePhone,
            representativeEmail,
            logoUrl,
            bannerUrl,
            websiteUrl,
            status,
            rejectionReason,
            scheduledDeletionAt,
            rejectedAt,
            approvedAt,
            approvedBy,
            rejectedBy,
            createdAt,
            updatedAt
        );
    }

    public static BrandProfile create(
        UUID id,
        UUID userId,
        BrandName brandName,
        String taxCode,
        String description,
        String phone,
        Address officeAddress,
        String representativeName,
        String representativePhone,
        String representativeEmail,
        String logoUrl,
        String bannerUrl,
        String websiteUrl,
        Instant now
    ) {
        return new BrandProfile(
            id,
            userId,
            brandName,
            taxCode,
            description,
            phone,
            officeAddress,
            representativeName,
            representativePhone,
            representativeEmail,
            logoUrl,
            bannerUrl,
            websiteUrl,
            BrandProfileStatus.PENDING_APPROVAL,
            null,
            null,
            null,
            null,
            null,
            null,
            now,
            now
        );
    }

    public void startReview(Instant now) {
        ensureStatus(BrandProfileStatus.PENDING_APPROVAL);
        this.status = BrandProfileStatus.UNDER_REVIEW;
        touch(now);
    }

    public void requestRevision(String reason, Instant now) {
        ensureStatus(
            BrandProfileStatus.UNDER_REVIEW,
            BrandProfileStatus.NEEDS_REVISION,
            BrandProfileStatus.READY_FOR_FINAL_REVIEW
        );
        this.status = BrandProfileStatus.NEEDS_REVISION;
        this.rejectionReason = requireNonBlank(reason, "revision reason is required");
        touch(now);
    }

    public void markReadyForFinalReview(Instant now) {
        ensureStatus(BrandProfileStatus.NEEDS_REVISION, BrandProfileStatus.UNDER_REVIEW);
        this.status = BrandProfileStatus.READY_FOR_FINAL_REVIEW;
        this.rejectionReason = null;
        touch(now);
    }

    public void approve(UUID approvedBy, Instant now) {
        ensureStatus(
            BrandProfileStatus.READY_FOR_FINAL_REVIEW,
            BrandProfileStatus.PENDING_APPROVAL,
            BrandProfileStatus.UNDER_REVIEW
        );
        this.status = BrandProfileStatus.ACTIVE;
        this.rejectionReason = null;
        this.approvedAt = now;
        this.approvedBy = approvedBy;
        this.rejectedAt = null;
        this.rejectedBy = null;
        this.scheduledDeletionAt = null;
        touch(now);
    }

    public void reject(String reason, UUID rejectedBy, Instant now) {
        this.status = BrandProfileStatus.REJECTED;
        this.rejectionReason = requireNonBlank(reason, "rejection reason is required");
        this.rejectedAt = now;
        this.rejectedBy = rejectedBy;
        this.approvedAt = null;
        this.approvedBy = null;
        this.scheduledDeletionAt = now.plus(REJECTION_GRACE_PERIOD);
        touch(now);
    }

    public void activate(Instant now) {
        this.status = BrandProfileStatus.ACTIVE;
        touch(now);
    }

    public void lock(Instant now) {
        this.status = BrandProfileStatus.LOCKED;
        touch(now);
    }

    public void disable(Instant now) {
        this.status = BrandProfileStatus.DISABLED;
        touch(now);
    }

    public void markDeleted(Instant now) {
        this.status = BrandProfileStatus.DELETED;
        this.scheduledDeletionAt = null;
        touch(now);
    }

    private void ensureProfileEditable() {
        if (!BrandAccessPolicy.canEditProfile(status)) {
            throw new BusinessException(ErrorCode.BRAND_ACCESS_DENIED);
        }
    }

    public void ensureActive() {
        if (status != BrandProfileStatus.ACTIVE) {
            throw new BusinessException(status.toAccessDeniedErrorCode());
        }
    }

    public void updateBrandName(BrandName brandName, Instant now) {
        ensureProfileEditable();
        this.brandName = Objects.requireNonNull(brandName, "brandName is required");
        touch(now);
    }

    public void updateTaxCode(String taxCode, Instant now) {
        ensureProfileEditable();
        this.taxCode = taxCode;
        touch(now);
    }

    public void updateDescription(String description, Instant now) {
        ensureProfileEditable();
        this.description = description;
        touch(now);
    }

    public void updatePhone(String phone, Instant now) {
        ensureProfileEditable();
        this.phone = Objects.requireNonNull(phone, "phone is required");
        touch(now);
    }

    public void updateOfficeAddress(Address officeAddress, Instant now) {
        ensureProfileEditable();
        this.officeAddress = officeAddress != null ? officeAddress : Address.empty();
        touch(now);
    }

    public void updateRepresentativeName(String representativeName, Instant now) {
        ensureProfileEditable();
        this.representativeName = Objects.requireNonNull(representativeName, "representativeName is required");
        touch(now);
    }

    public void updateRepresentativePhone(String representativePhone, Instant now) {
        ensureProfileEditable();
        this.representativePhone = Objects.requireNonNull(representativePhone, "representativePhone is required");
        touch(now);
    }

    public void updateRepresentativeEmail(String representativeEmail, Instant now) {
        ensureProfileEditable();
        this.representativeEmail = Objects.requireNonNull(representativeEmail, "representativeEmail is required");
        touch(now);
    }

    public void updateLogoUrl(String logoUrl, Instant now) {
        ensureProfileEditable();
        this.logoUrl = logoUrl;
        touch(now);
    }

    public void updateBannerUrl(String bannerUrl, Instant now) {
        ensureProfileEditable();
        this.bannerUrl = bannerUrl;
        touch(now);
    }

    public void updateWebsiteUrl(String websiteUrl, Instant now) {
        ensureProfileEditable();
        this.websiteUrl = websiteUrl;
        touch(now);
    }

    private void ensureStatus(BrandProfileStatus... allowedStatuses) {
        Set<BrandProfileStatus> allowed = Set.of(allowedStatuses);
        if (!allowed.contains(status)) {
            throw new BusinessException(ErrorCode.PROFILE_STATUS_INVALID);
        }
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.REJECTION_REASON_REQUIRED);
        }
        return value.trim();
    }

    private void touch(Instant now) {
        this.updatedAt = Objects.requireNonNull(now, "now is required");
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public BrandName getBrandName() {
        return brandName;
    }

    public String getDescription() {
        return description;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public String getPhone() {
        return phone;
    }

    public Address getOfficeAddress() {
        return officeAddress;
    }

    public String getRepresentativeName() {
        return representativeName;
    }

    public String getRepresentativePhone() {
        return representativePhone;
    }

    public String getRepresentativeEmail() {
        return representativeEmail;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public BrandProfileStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Instant getScheduledDeletionAt() {
        return scheduledDeletionAt;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public UUID getRejectedBy() {
        return rejectedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
