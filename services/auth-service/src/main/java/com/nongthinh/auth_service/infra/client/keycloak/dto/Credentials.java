package com.nongthinh.auth_service.infra.client.keycloak.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Credentials {
    private String type;
    private String value;
    private boolean temporary;
    
}
