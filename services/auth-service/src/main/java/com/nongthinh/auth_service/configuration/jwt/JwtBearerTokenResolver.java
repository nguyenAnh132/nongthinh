package com.nongthinh.auth_service.configuration.jwt;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

import com.nongthinh.auth_service.configuration.property.AuthCookieProperties;

import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Component
@EnableConfigurationProperties(AuthCookieProperties.class)
@RequiredArgsConstructor
public class JwtBearerTokenResolver implements BearerTokenResolver {

    private static final DefaultBearerTokenResolver DEFAULT_BEARER_TOKEN_RESOLVER = new DefaultBearerTokenResolver();
    private final AuthCookieProperties authCookieProperties;

    @Override
    public String resolve(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(authCookieProperties.accessTokenName())) {
                    return cookie.getValue();
                }
            }
        }
        return DEFAULT_BEARER_TOKEN_RESOLVER.resolve(request);
    }
}
