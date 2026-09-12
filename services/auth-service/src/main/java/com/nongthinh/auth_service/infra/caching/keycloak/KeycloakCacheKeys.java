package com.nongthinh.auth_service.infra.caching.keycloak;

public final class KeycloakCacheKeys {

    private static final String PREFIX = "auth:keycloak:";

    public static String clientToken() {
        return PREFIX + "clientToken";
    }

    public static String role(String role) {
        return PREFIX + "role:" + role;
    }

}
