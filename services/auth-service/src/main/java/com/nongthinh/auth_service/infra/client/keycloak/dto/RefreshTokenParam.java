package com.nongthinh.auth_service.infra.client.keycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenParam {
    private String grant_type;
    private String client_id;
    private String client_secret;
    private String refresh_token;
}