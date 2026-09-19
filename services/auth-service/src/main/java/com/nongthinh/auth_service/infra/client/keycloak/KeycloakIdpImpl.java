package com.nongthinh.auth_service.infra.client.keycloak;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.nongthinh.auth_service.application.port.out.ClockProvider;
import com.nongthinh.auth_service.infra.client.keycloak.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdp;
import com.nongthinh.auth_service.application.port.out.keycloak.RoleRecord;
import com.nongthinh.auth_service.application.view.RefreshTokenView;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.infra.exception.InfrastructureException;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component(value = "keycloakIdpImpl")
@RequiredArgsConstructor
@Slf4j
public class KeycloakIdpImpl implements KeycloakIdp {

    @Override
    public com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdentity getIdentity(UUID keycloakId, String token) {
        try {
            var response = keycloakClient.getIdentity(token, keycloakId.toString());
            return new com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdentity(
                    UUID.fromString(response.id()), response.email(), readApplicationId(response.attributes()), response.enabled());
        } catch (FeignException ex) {
            log.warn("[Infra - KeycloakIdentity] Read failed | keycloakId={} upstreamStatus={}", keycloakId, ex.status());
            throw new InfrastructureException(ErrorCode.KEYCLOAK_IDENTITY_SYNC_FAILED);
        } catch (IllegalArgumentException ex) {
            log.warn("[Infra - KeycloakIdentity] Invalid identity response | keycloakId={}", keycloakId);
            throw new InfrastructureException(ErrorCode.KEYCLOAK_IDENTITY_SYNC_FAILED);
        }
    }

    @Override
    public void updateNongThinhIdUser(UUID keycloakId, UUID applicationUserId, String token) {
        String stage = "read_current_identity";
        try {
            var response = keycloakClient.getIdentity(token, keycloakId.toString());
            UUID existing = readApplicationId(response.attributes());
            if (existing != null && !existing.equals(applicationUserId)) {
                throw new com.nongthinh.auth_service.domain.exception.BusinessException(ErrorCode.REGISTRATION_IDENTITY_CONFLICT);
            }
            if (applicationUserId.equals(existing)) return;
            var attributes = new java.util.LinkedHashMap<String, List<String>>();
            if (response.attributes() != null) attributes.putAll(response.attributes());
            attributes.put(NONGTHINH_ID_ATTRIBUTE, List.of(applicationUserId.toString()));
            // Keycloak validates the supplied profile when attributes are present, including root fields.
            // Preserve values read from Keycloak; this operation only changes the application identity.
            KeycloakUserUpdateNongThinhIdParam param = new KeycloakUserUpdateNongThinhIdParam(
                    response.username(), response.email(), response.firstName(), response.lastName(), attributes
            );
            stage = "update_attributes";
            keycloakClient.updateNongThinhIdUser(token, keycloakId.toString(), param);
            stage = "verify_attributes";
            if (!applicationUserId.equals(getIdentity(keycloakId, token).applicationUserId())) {
                log.warn("[Infra - KeycloakIdentity] Updated identity not visible on read-back | keycloakId={} userId={} stage={}",
                        keycloakId, applicationUserId, stage);
                throw new InfrastructureException(ErrorCode.KEYCLOAK_IDENTITY_SYNC_FAILED);
            }
        } catch (FeignException ex) {
            log.warn("[Infra - KeycloakIdentity] Link failed | keycloakId={} userId={} stage={} upstreamStatus={}",
                    keycloakId, applicationUserId, stage, ex.status());
            throw new InfrastructureException(ErrorCode.KEYCLOAK_IDENTITY_SYNC_FAILED);
        } catch (IllegalArgumentException ex) {
            log.warn("[Infra - KeycloakIdentity] Invalid identity response | keycloakId={} stage={}", keycloakId, stage);
            throw new InfrastructureException(ErrorCode.KEYCLOAK_IDENTITY_SYNC_FAILED);
        }
    }

    private UUID readApplicationId(Map<String, List<String>> attributes) {
        var values = attributes == null ? null : attributes.get(NONGTHINH_ID_ATTRIBUTE);
        if (values == null || values.isEmpty()) return null;
        if (values.size() != 1) throw new IllegalArgumentException("Ambiguous application identity");
        return UUID.fromString(values.getFirst());
    }

    private static final String CREDENTIALS_TYPE = "password";
    private static final String EXCHANGE_CLIENT_TOKEN_GRANT_TYPE = "client_credentials";
    private static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
    private static final String NONGTHINH_ID_ATTRIBUTE = "nongthinh_id";

    private final KeycloakClient keycloakClient;

    private final ClockProvider clockProvider;

    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;
    @Value("${keycloak.scope}")
    private String scope;

    @Override
    public String createUser(UUID userId, String email, String password, boolean temporary, boolean enabled, String token) {

        long start = System.currentTimeMillis();

        KeycloakUserRegisterParam request = new KeycloakUserRegisterParam(
                email,
                enabled,
                false,
                Map.of(NONGTHINH_ID_ATTRIBUTE, List.of(userId.toString())),
                List.of(new Credentials(CREDENTIALS_TYPE, password, temporary))
        );
        try {
            ResponseEntity<?> response = keycloakClient.createUser(token, request);

            String keycloakUserId = getKeycloakUserId(response);

            log.info(
                    "[INFRA - Keycloak] Keycloak user account created successfully | keycloakUserId={} userId={} durationMs={}",
                    keycloakUserId,
                    userId,
                    System.currentTimeMillis() - start
            );

            return keycloakUserId;
        } catch (FeignException ex) {
            throw new InfrastructureException(ErrorCode.KEYCLOAK_USER_CREATION_FAILED, ex);
        }
    }

    private String getKeycloakUserId(ResponseEntity<?> response) {
        String location = response.getHeaders().get("Location").getFirst();
        if (location.isEmpty()) {
            throw new InfrastructureException(ErrorCode.KEYCLOAK_USER_ID_NOT_FOUND);
        }
        String[] split = location.split("/");
        return split[split.length - 1];
    }

    @Override
    public String exchangeClientToken() {
        TokenExchangeParam param = new TokenExchangeParam(
                EXCHANGE_CLIENT_TOKEN_GRANT_TYPE,
                clientId,
                clientSecret,
                scope);
        try {
            TokenExchangeResponse response = keycloakClient.exchangeClientToken(param);
            return response.getAccessToken();
        } catch (FeignException ex) {
            throw new InfrastructureException(ErrorCode.KEYCLOAK_EXCHANGE_CLIENT_TOKEN_FAILED);
        }
    }

    @Override
    public RefreshTokenView refreshToken(String refreshToken) {
        TokenExchangeResponse response;
        try {
             response = keycloakClient.refreshToken(new RefreshTokenParam(
                REFRESH_TOKEN_GRANT_TYPE,
                clientId,
                clientSecret,
                refreshToken
            ));
        } catch (FeignException e) {
            throw new InfrastructureException(ErrorCode.REFRESH_TOKEN_UNSUCCESSFUL);
        }

        Instant now = clockProvider.now();
        Instant accessExpiresAt = now.plusSeconds(response.getExpiresIn());
        Instant refreshExpiresAt = now.plusSeconds(response.getRefreshExpiresIn());

        return new RefreshTokenView(
            response.getAccessToken(),
            response.getRefreshToken(),
            accessExpiresAt,
            refreshExpiresAt
        );
    }

    @Override
    public void logout(String refreshToken) {
        try {
            keycloakClient.logout(new LogoutParam(refreshToken, clientId, clientSecret));
        } catch (FeignException ex) {
            throw new InfrastructureException(ErrorCode.KEYCLOAK_LOGOUT_FAILED);
        }
    }

    @Override
    public RoleRecord getRoleByName(String name, String token) {

        try {
            RoleResponse response = findRole(name, token);
            return new RoleRecord(
                response.getId(),
                response.getName(),
                response.isComposite(),
                response.isClientRole(),
                response.getContainerId()
            );
        } catch (FeignException ex) {
            throw new InfrastructureException(ErrorCode.KEYCLOAK_GET_ROLE_BY_NAME_FAILED);
        }
    }

    @Override
    public void assignRealmRoles(String keycloakUserId, java.util.Collection<String> roleNames, String token) {
        try {
            List<RoleMapping> mappings = roleNames.stream()
                    .map(name -> {
                        RoleRecord role = getRoleByName(name, token);
                        return new RoleMapping(
                                role.id(),
                                role.name(),
                                role.composite(),
                                role.clientRole(),
                                role.containerId());
                    })
                    .toList();
            keycloakClient.roleMapping(token, keycloakUserId, mappings);
        } catch (FeignException ex) {
            throw new InfrastructureException(ErrorCode.KEYCLOAK_ROLE_MAPPING_FAILED, ex);
        }
    }
    private RoleResponse findRole(String name, String token) {
        try {
            return keycloakClient.getRoleByName(token, name);
        } catch (FeignException.NotFound ex) {
            if (!com.nongthinh.auth_service.common.constant.RoleConstant.ROLE_BRAND_PENDING.equals(name)) throw ex;
            // Provision a restricted role without inherited application permissions.
            try {
                keycloakClient.createRole(token, Map.of("name", name));
            } catch (FeignException.Conflict alreadyCreated) {
                // Another instance provisioned the role concurrently.
            }
            return keycloakClient.getRoleByName(token, name);
        }
    }

    @Override
    public java.util.Set<String> getRealmRoleNames(String keycloakUserId, String token) {
        try {
            return keycloakClient.getRealmRoles(token, keycloakUserId).stream()
                    .map(RoleResponse::getName).collect(java.util.stream.Collectors.toSet());
        } catch (FeignException ex) {
            throw new InfrastructureException(ErrorCode.KEYCLOAK_ROLE_MAPPING_FAILED, ex);
        }
    }

    @Override
    public void removeRealmRoles(String keycloakUserId, java.util.Collection<String> roleNames, String token) {
        try {
            List<RoleMapping> roles = roleNames.stream().map(name -> {
                RoleRecord role = getRoleByName(name, token);
                return new RoleMapping(role.id(), role.name(), role.composite(), role.clientRole(), role.containerId());
            }).toList();
            keycloakClient.removeRealmRoles(token, keycloakUserId, roles);
        } catch (FeignException ex) {
            throw new InfrastructureException(ErrorCode.KEYCLOAK_ROLE_MAPPING_FAILED, ex);
        }
    }
}
