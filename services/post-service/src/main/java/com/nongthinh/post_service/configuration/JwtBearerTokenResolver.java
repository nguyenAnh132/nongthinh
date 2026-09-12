package com.nongthinh.post_service.configuration;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtBearerTokenResolver implements BearerTokenResolver {
    private static final DefaultBearerTokenResolver HEADER_RESOLVER = new DefaultBearerTokenResolver();
    private final AuthCookieProperties cookieProperties;

    @Override
    public String resolve(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieProperties.accessTokenName())) {
                    return cookie.getValue();
                }
            }
        }
        return HEADER_RESOLVER.resolve(request);
    }
}
