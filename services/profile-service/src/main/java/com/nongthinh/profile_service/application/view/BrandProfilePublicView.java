package com.nongthinh.profile_service.application.view;

import java.util.UUID;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;

public record BrandProfilePublicView(
        UUID id,
        String brandName,
        String description,
        String officeProvinceId,
        UUID officeCommuneId,
        String phone,
        String representativeName,
        String logoUrl,
        String bannerUrl,
        String websiteUrl,
        boolean verified
) {
    public static BrandProfilePublicView from(BrandProfile profile) {
        return new BrandProfilePublicView(
                profile.getId(),
                profile.getBrandName().getValue(),
                profile.getDescription(),
                profile.getOfficeAddress().getProvinceId(),
                profile.getOfficeAddress().getCommuneId(),
                profile.getPhone(),
                profile.getRepresentativeName(),
                profile.getLogoUrl(),
                profile.getBannerUrl(),
                profile.getWebsiteUrl(),
                profile.getStatus() == com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandProfileStatus.ACTIVE
        );
    }
}
