package com.nongthinh.auth_service.configuration.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakCacheProperties {

    private int clientTokenTtlSeconds = 600;
    private int roleTtlSeconds = 3600;

}
