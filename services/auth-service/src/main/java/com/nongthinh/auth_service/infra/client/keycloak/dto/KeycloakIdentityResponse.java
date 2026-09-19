package com.nongthinh.auth_service.infra.client.keycloak.dto;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakIdentityResponse(String id, String username, String email, String firstName, String lastName,
        boolean enabled, Map<String, List<String>> attributes) {
}
