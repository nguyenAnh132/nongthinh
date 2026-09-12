package com.nongthinh.auth_service.common.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.configuration.property.AuthCookieProperties;
import com.nongthinh.auth_service.infra.exception.InfrastructureException;

@Component
@RequiredArgsConstructor
public class RefreshTokenResolver {

    private final AuthCookieProperties authCookieProperties;

    public String resolve(HttpServletRequest httpRequest) {
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies == null) {
            throw new InfrastructureException(ErrorCode.INVALID_TOKEN);
        }

        for (Cookie cookie : cookies) {
            if (authCookieProperties.refreshTokenName().equals(cookie.getName())
                    && cookie.getValue() != null
                    && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        throw new InfrastructureException(ErrorCode.INVALID_TOKEN);
    }
}
