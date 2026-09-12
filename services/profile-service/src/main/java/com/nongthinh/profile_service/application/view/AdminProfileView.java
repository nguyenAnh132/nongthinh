package com.nongthinh.profile_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.profile_service.domain.adminprofile.AdminProfile;

public record AdminProfileView(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String phone,
        String avatarUrl,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static AdminProfileView from(AdminProfile profile) {
        return new AdminProfileView(
                profile.getId(),
                profile.getUserId(),
                profile.getFirstName().getValue(),
                profile.getLastName().getValue(),
                profile.getPhone(),
                profile.getAvatarUrl(),
                profile.getStatus().getValue(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
