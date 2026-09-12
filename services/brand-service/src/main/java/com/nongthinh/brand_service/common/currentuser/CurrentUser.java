package com.nongthinh.brand_service.common.currentuser;

import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class CurrentUser {
    private UUID userId;
    private String keycloakId;
    private String email;
    private Set<String> roles;
    private String adminGroup;
    private Set<String> permissions;

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
