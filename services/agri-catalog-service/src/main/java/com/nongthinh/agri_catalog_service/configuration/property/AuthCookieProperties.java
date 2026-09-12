package com.nongthinh.agri_catalog_service.configuration.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cookie")
public record AuthCookieProperties(
        boolean secure,
        boolean httpOnly,
        String accessTokenName,
        String refreshTokenName,
        String sameSite,
        String accessPath,
        String refreshPath
) {
}
