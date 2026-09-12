package com.nongthinh.auth_service.infra.client.keycloak.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.util.Map;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse {

    private UUID id;

    private String name;

    private String description;

    private boolean composite;

    private boolean clientRole;

    private UUID containerId;

    private Map<String, List<String>> attributes;
}