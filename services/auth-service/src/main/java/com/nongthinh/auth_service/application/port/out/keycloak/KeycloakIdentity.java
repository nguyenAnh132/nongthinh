package com.nongthinh.auth_service.application.port.out.keycloak;

import java.util.UUID;

public record KeycloakIdentity(UUID id, String email, UUID applicationUserId, boolean enabled) {
}
