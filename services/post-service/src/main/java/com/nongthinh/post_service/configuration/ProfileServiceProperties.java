package com.nongthinh.post_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "post.profile-service")
public record ProfileServiceProperties(
        @DefaultValue("http://localhost:9092/profile") String url,
        @DefaultValue("2000") int connectTimeoutMs,
        @DefaultValue("3000") int readTimeoutMs
) {
    public ProfileServiceProperties {
        if (url == null || url.isBlank() || connectTimeoutMs < 1 || readTimeoutMs < 1) {
            throw new IllegalArgumentException("Profile service URL and positive timeouts are required");
        }
    }
}
