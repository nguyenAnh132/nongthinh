package com.nongthinh.rice_disease_diagnosis_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "brand-access.profile-service")
public record BrandAccessProperties(
        @DefaultValue("http://localhost:9092/profile") String url,
        @DefaultValue("2000") int connectTimeoutMs,
        @DefaultValue("3000") int readTimeoutMs) {
    public BrandAccessProperties {
        if (url == null || url.isBlank() || connectTimeoutMs < 1 || readTimeoutMs < 1) {
            throw new IllegalArgumentException("Profile service URL and positive timeouts are required");
        }
    }
}
