package com.nongthinh.profile_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;

public record BrandProfileView(
        UUID id,
        UUID userId,
        String brandName,
        String taxCode,
        String description,
        String phone,
        String officeProvinceId,
        UUID officeCommuneId,
        String officeAddressDetail,
        String representativeName,
        String representativePhone,
        String representativeEmail,
        String logoUrl,
        String bannerUrl,
        String websiteUrl,
        String status,
        String rejectionReason,
        Instant scheduledDeletionAt,
        Instant rejectedAt,
        Instant approvedAt,
        UUID approvedBy,
        UUID rejectedBy,
        Instant createdAt,
        Instant updatedAt
) {
    public static BrandProfileView from(BrandProfile profile) {
        return new BrandProfileView(
                profile.getId(),
                profile.getUserId(),
                profile.getBrandName().getValue(),
                profile.getTaxCode(),
                profile.getDescription(),
                profile.getPhone(),
                profile.getOfficeAddress().getProvinceId(),
                profile.getOfficeAddress().getCommuneId(),
                profile.getOfficeAddress().getDetail(),
                profile.getRepresentativeName(),
                profile.getRepresentativePhone(),
                profile.getRepresentativeEmail(),
                profile.getLogoUrl(),
                profile.getBannerUrl(),
                profile.getWebsiteUrl(),
                profile.getStatus().getValue(),
                profile.getRejectionReason(),
                profile.getScheduledDeletionAt(),
                profile.getRejectedAt(),
                profile.getApprovedAt(),
                profile.getApprovedBy(),
                profile.getRejectedBy(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
