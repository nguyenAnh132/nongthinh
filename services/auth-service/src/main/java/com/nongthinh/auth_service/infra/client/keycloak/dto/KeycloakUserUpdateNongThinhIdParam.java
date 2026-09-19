package com.nongthinh.auth_service.infra.client.keycloak.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakUserUpdateNongThinhIdParam {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Map<String, List<String>> attributes;
}
