package com.nongthinh.auth_service.configuration;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.nongthinh.auth_service.configuration.property.AuthCookieProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final AuthCookieProperties authCookieProperties;

    @Value("${app.frontend-base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauth2Token.getAuthorizedClientRegistrationId(),
                oauth2Token.getName());

        if (client == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "OAuth2 authorized client not found");
            return;
        }

        OAuth2AccessToken accessToken = client.getAccessToken();
        OAuth2RefreshToken refreshToken = client.getRefreshToken();
        log.info("Access Expires At: {}", accessToken.getExpiresAt());
        log.info("Refresh Expires At: {}", refreshToken.getExpiresAt());

        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie(
                        authCookieProperties.accessTokenName(),
                        accessToken.getTokenValue(),
                        authCookieProperties.accessPath(),
                        toMaxAgeSeconds(accessToken.getExpiresAt())).toString());

        if (refreshToken != null) {
            response.addHeader(HttpHeaders.SET_COOKIE,
                    buildCookie(
                            authCookieProperties.refreshTokenName(),
                            refreshToken.getTokenValue(),
                            authCookieProperties.refreshPath(),
                            authCookieProperties.refreshMaxAgeSeconds()
                    ).toString());
        }

        request.getSession(false);
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        response.sendRedirect(frontendBaseUrl + "/");
    }

    private ResponseCookie buildCookie(String name, String value, String path, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .path(path != null ? path : "/")
                .httpOnly(authCookieProperties.httpOnly())
                .secure(authCookieProperties.secure())
                .sameSite(authCookieProperties.sameSite())
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }

    private long toMaxAgeSeconds(Instant expiresAt) {
        if (expiresAt == null) {
            return Duration.ofMinutes(5).getSeconds();
        }
        long seconds = Duration.between(Instant.now(), expiresAt).getSeconds();
        return Math.max(seconds, 1);
    }
}