package com.nongthinh.rice_disease_diagnosis_service.configuration.jwt;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;
import com.nongthinh.rice_disease_diagnosis_service.configuration.property.AuthCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtBearerTokenResolver implements BearerTokenResolver {
    private final AuthCookieProperties authCookieProperties;
    private final DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest request) {
        if (request.getCookies() != null && authCookieProperties.accessTokenName() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (authCookieProperties.accessTokenName().equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return delegate.resolve(request);
    }
}
