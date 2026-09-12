package com.nongthinh.profile_service.application.view;

import java.util.UUID;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;

public record FarmerProfilePublicView(
        UUID id,
        String firstName,
        String lastName,
        String gender,
        String provinceId,
        UUID communeId,
        String avatarUrl
) {
    public static FarmerProfilePublicView from(FarmerProfile profile) {
        return new FarmerProfilePublicView(
                profile.getId(),
                profile.getFirstName().getValue(),
                profile.getLastName().getValue(),
                profile.getGender().getValue(),
                profile.getAddress().getProvinceId(),
                profile.getAddress().getCommuneId(),
                profile.getAvatarUrl()
        );
    }
}
