package com.nongthinh.profile_service.application.command.brand;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandName;
import com.nongthinh.profile_service.domain.shared.valueobject.Address;

public record BrandProfileCreationCommand(
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
    String websiteUrl
) {

    public static BrandProfile toBrandProfile(BrandProfileCreationCommand command, Instant now, UUID id) {
        return BrandProfile.create(
            id,
            command.userId(),
            BrandName.of(command.brandName()),
            command.taxCode(),
            command.description(),
            command.phone(),
            Address.of(command.officeProvinceId(), command.officeCommuneId(), command.officeAddressDetail()),
            command.representativeName(),
            command.representativePhone(),
            command.representativeEmail(),
            command.logoUrl(),
            command.bannerUrl(),
            command.websiteUrl(),
            now
        );
    }
}
