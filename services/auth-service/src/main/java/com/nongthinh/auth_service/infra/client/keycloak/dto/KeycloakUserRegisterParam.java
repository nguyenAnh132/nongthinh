package com.nongthinh.auth_service.infra.client.keycloak.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakUserRegisterParam {
    private String email;
    private boolean enabled;
    private boolean emailVerified;
    private Map<String, List<String>> attributes;
    private List<Credentials> credentials;
}
