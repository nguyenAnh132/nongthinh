package com.nongthinh.post_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.api-key.clients.agri-catalog-service")
public record AgriCatalogServiceProperties(
        String url,
        int connectTimeoutMs,
        int readTimeoutMs
) {
    public AgriCatalogServiceProperties {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("agri-catalog-service url is required");
        }
        if (connectTimeoutMs < 1 || readTimeoutMs < 1) {
            throw new IllegalArgumentException("agri-catalog-service timeouts must be positive");
        }
    }
}
