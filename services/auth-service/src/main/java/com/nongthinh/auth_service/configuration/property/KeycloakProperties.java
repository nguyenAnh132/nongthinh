package com.nongthinh.auth_service.configuration.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private String url;
    private String realm;

    public String buildIssuerUri() {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("keycloak.url must be configured");
        }
        if (realm == null || realm.isBlank()) {
            throw new IllegalStateException("keycloak.realm must be configured");
        }

        String baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return baseUrl + "/realms/" + realm;
    }
}
