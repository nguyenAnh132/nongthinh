package com.nongthinh.auth_service.application.command;

import java.util.UUID;

public record RegisterFarmerCommand(
    String email,
    String password,
    boolean temporary,
    boolean enabled,
    String firstName,
    String lastName,
    String gender,
    String phone,
    String provinceId,
    UUID communeId,
    String addressDetail,
    String avatarUrl
) {
}
