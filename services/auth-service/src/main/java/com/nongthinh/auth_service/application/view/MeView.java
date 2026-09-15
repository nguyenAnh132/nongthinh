package com.nongthinh.auth_service.application.view;

import java.util.Set;
import java.util.UUID;

public record MeView(
    UUID userId,
    String email,
    String role,
    String adminGroup,
    Set<String> permissions,
    ProfileView profile,
    MeFlags flags
) {

}
