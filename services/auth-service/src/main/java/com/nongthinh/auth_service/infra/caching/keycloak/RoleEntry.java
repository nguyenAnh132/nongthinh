package com.nongthinh.auth_service.infra.caching.keycloak;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleEntry {

    private UUID id;
    private String name;
    private boolean composite;
    private boolean clientRole;
    private UUID containerId;

}
