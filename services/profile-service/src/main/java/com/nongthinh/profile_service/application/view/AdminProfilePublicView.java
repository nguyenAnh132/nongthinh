package com.nongthinh.profile_service.application.view;

import java.util.UUID;
import com.nongthinh.profile_service.domain.adminprofile.AdminProfile;

public record AdminProfilePublicView(
        UUID id,
        String firstName,
        String lastName,
        String avatarUrl
) {
    public static AdminProfilePublicView from(AdminProfile profile) {
        return new AdminProfilePublicView(
                profile.getId(),
                profile.getFirstName().getValue(),
                profile.getLastName().getValue(),
                profile.getAvatarUrl()
        );
    }
}
