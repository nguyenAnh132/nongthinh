package com.nongthinh.profile_service.common.currentuser;

import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CurrentUser {

    private final UUID userId;
    private final String keycloakId;
    private final String email;
    private final Set<String> roles;
    private final Set<String> permissions;

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
