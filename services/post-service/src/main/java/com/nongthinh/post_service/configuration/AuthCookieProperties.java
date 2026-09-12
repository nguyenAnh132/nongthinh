package com.nongthinh.post_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cookie")
public record AuthCookieProperties(String accessTokenName) {
}
