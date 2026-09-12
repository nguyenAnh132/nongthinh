package com.nongthinh.profile_service.application.command.farmer;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;
import com.nongthinh.profile_service.domain.farmerprofile.valueobject.Gender;
import com.nongthinh.profile_service.domain.shared.valueobject.Address;
import com.nongthinh.profile_service.domain.shared.valueobject.PersonName;

public record FarmerProfileCreationCommand(
    UUID userId,
    String firstName,
    String lastName,
    String gender,
    String phone,
    String provinceId,
    UUID communeId,
    String addressDetail,
    String avatarUrl
) {

    public static FarmerProfile toFarmerProfile(FarmerProfileCreationCommand command, Instant now, UUID id) {
        return FarmerProfile.create(
            id,
            command.userId(),
            PersonName.of(command.firstName()),
            PersonName.of(command.lastName()),
            Gender.fromString(command.gender()),
            command.phone(),
            Address.of(command.provinceId(), command.communeId(), command.addressDetail()),
            command.avatarUrl(),
            now
        );
    }
}
