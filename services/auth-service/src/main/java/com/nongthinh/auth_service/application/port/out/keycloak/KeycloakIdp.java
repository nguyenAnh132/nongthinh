package com.nongthinh.auth_service.application.port.out.keycloak;

import java.util.UUID;
import com.nongthinh.auth_service.application.view.RefreshTokenView;

public interface KeycloakIdp {

    KeycloakIdentity getIdentity(UUID keycloakId, String token);

    void updateNongThinhIdUser(UUID keycloakId, UUID applicationUserId, String token);

    String createUser(UUID userId, String email, String password, boolean temporary, boolean enabled, String token);

    String exchangeClientToken();

    RefreshTokenView refreshToken(String refreshToken);

    void logout(String refreshToken);

    RoleRecord getRoleByName(String name, String token);

    void assignRealmRoles(String keycloakUserId, java.util.Collection<String> roleNames, String token);

    java.util.Set<String> getRealmRoleNames(String keycloakUserId, String token);

    void removeRealmRoles(String keycloakUserId, java.util.Collection<String> roleNames, String token);

}
