package com.nongthinh.profile_service.application.command;

import java.util.UUID;

public record CompleteRegistrationProfileCommand(UUID userId, String role, String firstName, String lastName,
        String gender, String phone, String brandName, String representativeName, String representativePhone,
        String representativeEmail) {
}
