package com.nongthinh.auth_service.configuration;

import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import com.nongthinh.auth_service.common.constant.ObservabilityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UserMdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            putUserIdIfPresent();
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(ObservabilityConstants.MDC_USER_ID);
        }
    }

    private void putUserIdIfPresent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        Jwt jwt = extractJwt(authentication);
        if (jwt == null) {
            return;
        }

        String userId = jwt.getClaimAsString(ObservabilityConstants.JWT_USER_ID_CLAIM);
        if (userId != null && !userId.isBlank()) {
            MDC.put(ObservabilityConstants.MDC_USER_ID, userId);
        }
    }

    private Jwt extractJwt(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        return null;
    }
}