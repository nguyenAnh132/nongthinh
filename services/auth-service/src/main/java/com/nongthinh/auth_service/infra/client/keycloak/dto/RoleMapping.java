package com.nongthinh.auth_service.infra.client.keycloak.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleMapping {

    private UUID id;
    private String name;
    private boolean composite;
    private boolean clientRole;
    private UUID containerId;
}
