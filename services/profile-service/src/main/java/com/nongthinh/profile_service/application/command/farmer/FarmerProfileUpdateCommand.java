package com.nongthinh.profile_service.application.command.farmer;

import java.util.UUID;

public record FarmerProfileUpdateCommand(
    String firstName,
    String lastName,
    String gender
) {
}
