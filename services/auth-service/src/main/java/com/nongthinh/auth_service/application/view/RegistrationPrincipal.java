package com.nongthinh.auth_service.application.view;

import java.util.Set;
import java.util.UUID;

// Constructed only from the resource server's authenticated JWT, never from a request body.
public record RegistrationPrincipal(UUID keycloakId, String email, UUID applicationUserId,
        String role, String adminGroup, Set<String> permissions) {
}
