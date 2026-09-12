package com.nongthinh.auth_service.application.port.out.keycloak;

import java.util.UUID;

public record RoleRecord(
    UUID id,
    String name,
    boolean composite,
    boolean clientRole,
    UUID containerId
) {

}
