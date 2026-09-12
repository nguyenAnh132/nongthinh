package com.nongthinh.auth_service.infra.client.keycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutParam {

    private String refresh_token;
    private String client_id;
    private String client_secret;

}
