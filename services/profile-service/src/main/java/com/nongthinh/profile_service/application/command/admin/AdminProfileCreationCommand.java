package com.nongthinh.profile_service.application.command.admin;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.profile_service.domain.adminprofile.AdminProfile;
import com.nongthinh.profile_service.domain.shared.valueobject.PersonName;

public record AdminProfileCreationCommand(
    UUID userId,
    String firstName,
    String lastName,
    String phone,
    String avatarUrl
) {

    public static AdminProfile toAdminProfile(AdminProfileCreationCommand command, Instant now, UUID id) {
        return AdminProfile.create(
            id,
            command.userId(),
            PersonName.of(command.firstName()),
            PersonName.of(command.lastName()),
            command.phone(),
            command.avatarUrl(),
            now
        );
    }
}
