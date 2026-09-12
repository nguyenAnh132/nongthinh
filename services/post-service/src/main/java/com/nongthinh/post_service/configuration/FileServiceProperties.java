package com.nongthinh.post_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.api-key.clients.file-service")
public record FileServiceProperties(
        String url,
        String apiKey,
        int connectTimeoutMs,
        int readTimeoutMs
) {
    public FileServiceProperties {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("file-service url is required");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("file-service api key is required");
        }
        if (connectTimeoutMs < 1 || readTimeoutMs < 1) {
            throw new IllegalArgumentException("file-service timeouts must be positive");
        }
    }
}
