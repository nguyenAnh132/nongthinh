package com.nongthinh.auth_service.infra.client.keycloak.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenExchangeParam {

    private String grant_type;
    private String client_id;
    private String client_secret;
    private String scope;

}
