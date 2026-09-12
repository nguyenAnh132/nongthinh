package com.nongthinh.profile_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;

public record FarmerProfileView(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String gender,
        String phone,
        String provinceId,
        String provinceName,
        UUID communeId,
        String communeName,
        String addressDetail,
        String avatarUrl,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static FarmerProfileView from(FarmerProfile profile) {
        return from(profile, AddressNamesView.empty());
    }

    public static FarmerProfileView from(FarmerProfile profile, AddressNamesView addressNames) {
        return new FarmerProfileView(
                profile.getId(),
                profile.getUserId(),
                profile.getFirstName().getValue(),
                profile.getLastName().getValue(),
                profile.getGender().getValue(),
                profile.getPhone(),
                profile.getAddress().getProvinceId(),
                addressNames.provinceName(),
                profile.getAddress().getCommuneId(),
                addressNames.communeName(),
                profile.getAddress().getDetail(),
                profile.getAvatarUrl(),
                profile.getStatus().getValue(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
