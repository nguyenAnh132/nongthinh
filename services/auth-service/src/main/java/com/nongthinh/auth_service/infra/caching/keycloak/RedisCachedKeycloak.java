package com.nongthinh.auth_service.infra.caching.keycloak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdp;
import com.nongthinh.auth_service.application.port.out.keycloak.RoleRecord;
import com.nongthinh.auth_service.application.view.RefreshTokenView;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.configuration.property.KeycloakCacheProperties;
import com.nongthinh.auth_service.infra.caching.RedisStringCache;
import com.nongthinh.auth_service.infra.exception.InfrastructureException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@Slf4j
public class RedisCachedKeycloak implements KeycloakIdp {

    @Override
    public com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdentity getIdentity(UUID keycloakId, String token) {
        return keycloakIdp.getIdentity(keycloakId, token);
    }

    @Override
    public void updateNongThinhIdUser(UUID keycloakId, UUID applicationUserId, String token) {
        keycloakIdp.updateNongThinhIdUser(keycloakId, applicationUserId, token);
    }

    private final KeycloakIdp keycloakIdp;
    private final RedisStringCache redisStringCache;
    private final KeycloakCacheProperties keycloakCacheProperties;
    private final ObjectMapper redisObjectMapper;

    public RedisCachedKeycloak(
            @Qualifier("keycloakIdpImpl") KeycloakIdp keycloakIdp,
            RedisStringCache redisStringCache,
            KeycloakCacheProperties keycloakCacheProperties,
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        this.keycloakIdp = keycloakIdp;
        this.redisStringCache = redisStringCache;
        this.keycloakCacheProperties = keycloakCacheProperties;
        this.redisObjectMapper = redisObjectMapper;
    }

    @Override
    public String createUser(UUID userId, String email, String password, boolean temporary, boolean enabled,
            String token) {
        return keycloakIdp.createUser(userId, email, password, temporary, enabled, token);
    }

    @Override
    public String exchangeClientToken() {
        log.info("[Infra - ClientToken] Get client token from cache");
        return redisStringCache.get(KeycloakCacheKeys.clientToken()).orElseGet(() -> {
            String token = keycloakIdp.exchangeClientToken();
            int ttl = keycloakCacheProperties.getClientTokenTtlSeconds();
            redisStringCache.put(KeycloakCacheKeys.clientToken(), token, Duration.ofSeconds(ttl));
            return token;
        });
    }

    @Override
    public RoleRecord getRoleByName(String name, String token) {
        Optional<String> cached = redisStringCache.get(KeycloakCacheKeys.role(name));
        if (cached.isPresent()) {
            Optional<RoleRecord> role = tryDeserializeRole(cached.get());
            if (role.isPresent()) {
                return role.get();
            }
            redisStringCache.invalidate(KeycloakCacheKeys.role(name));
        }
        return fetchAndCacheRole(name, token);
    }

    @Override
    public void logout(String refreshToken) {
        keycloakIdp.logout(refreshToken);
    }

    @Override
    public RefreshTokenView refreshToken(String refreshToken) {
        return keycloakIdp.refreshToken(refreshToken);
    }

    @Override
    public void assignRealmRoles(String keycloakUserId, java.util.Collection<String> roleNames, String token) {
        keycloakIdp.assignRealmRoles(keycloakUserId, roleNames, token);
    }

    @Override
    public java.util.Set<String> getRealmRoleNames(String keycloakUserId, String token) {
        return keycloakIdp.getRealmRoleNames(keycloakUserId, token);
    }

    @Override
    public void removeRealmRoles(String keycloakUserId, java.util.Collection<String> roleNames, String token) {
        keycloakIdp.removeRealmRoles(keycloakUserId, roleNames, token);
    }

    private RoleRecord fetchAndCacheRole(String name, String token) {
        RoleRecord role = keycloakIdp.getRoleByName(name, token);
        try {
            String json = redisObjectMapper.writeValueAsString(toRoleEntry(role));
            redisStringCache.put(
                    KeycloakCacheKeys.role(name),
                    json,
                    Duration.ofSeconds(keycloakCacheProperties.getRoleTtlSeconds()));
        } catch (JsonProcessingException e) {
            throw new InfrastructureException(ErrorCode.INTERNAL_ERROR, "Failed to serialize role", e);
        }
        return role;
    }

    private Optional<RoleRecord> tryDeserializeRole(String json) {
        try {
            RoleEntry entry = redisObjectMapper.readValue(json, RoleEntry.class);
            return Optional.of(toRoleRecord(entry));
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cached role, will refetch. cause={}", e.getMessage());
            return Optional.empty();
        }
    }

    private RoleEntry toRoleEntry(RoleRecord role) {
        return new RoleEntry(
                role.id(),
                role.name(),
                role.composite(),
                role.clientRole(),
                role.containerId());
    }

    private RoleRecord toRoleRecord(RoleEntry entry) {
        return new RoleRecord(
                entry.getId(),
                entry.getName(),
                entry.isComposite(),
                entry.isClientRole(),
                entry.getContainerId());
    }
}
