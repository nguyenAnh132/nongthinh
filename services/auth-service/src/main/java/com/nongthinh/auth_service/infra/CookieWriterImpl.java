package com.nongthinh.auth_service.infra;

import java.net.HttpCookie;
import java.time.Duration;
import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import com.nongthinh.auth_service.common.cookie.CookieWriter;
import com.nongthinh.auth_service.common.cookie.TokenCookie;
import com.nongthinh.auth_service.configuration.property.AuthCookieProperties;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CookieWriterImpl implements CookieWriter {

    private final AuthCookieProperties authCookieProperties;

    @Override
    public void clearTokenFromCookie(HttpServletResponse response) {
        Instant now = Instant.now();
        TokenCookie tokenCookie = new TokenCookie("", "", now, now);
        writeCookie(response, tokenCookie, now);
        
    }

    @Override
    public void setTokenToCookie(HttpServletResponse response, TokenCookie tokenCookie) {
        Instant now = Instant.now();
        writeCookie(response, tokenCookie, now);
    }

    private void writeCookie (HttpServletResponse response, TokenCookie tokenCookie, Instant now) {
        response.addHeader(
            HttpHeaders.SET_COOKIE,
            buildCookie(
                authCookieProperties.accessTokenName(),
                tokenCookie.accessToken(),
                authCookieProperties.accessPath(),
                tokenCookie.expiresIn(),
                now
            ).toString()
        );

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            buildCookie(
                authCookieProperties.refreshTokenName(),
                tokenCookie.refreshToken(),
                authCookieProperties.refreshPath(),
                tokenCookie.refreshExpiresIn(),
                now
            ).toString()
        );
    }

    private ResponseCookie buildCookie(
        String name, 
        String value, 
        String path, 
        Instant expiresIn, 
        Instant now
    ) {

        long maxAge = Duration.between(now, expiresIn).getSeconds();

        return ResponseCookie.from(name, value)
            .httpOnly(authCookieProperties.httpOnly())
            .sameSite(authCookieProperties.sameSite())
            .secure(authCookieProperties.secure())
            .path(path)
            .maxAge(maxAge)
            .build();
        
    }
}
