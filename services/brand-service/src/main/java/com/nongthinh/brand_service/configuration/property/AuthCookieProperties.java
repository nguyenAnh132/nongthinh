package com.nongthinh.brand_service.configuration.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cookie")
public record AuthCookieProperties(
        String accessTokenName
) {
}
