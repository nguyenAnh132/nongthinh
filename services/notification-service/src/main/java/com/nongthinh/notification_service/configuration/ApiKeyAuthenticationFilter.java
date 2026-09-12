package com.nongthinh.notification_service.configuration;

import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.common.response.ApiResponse;  
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String INTERNAL_PATH_PATTERN = "/internal/**";
    private static final String ROLE_INTERNAL = "ROLE_INTERNAL";

    private final ApiKeyProperties apiKeyProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !pathMatcher.match(INTERNAL_PATH_PATTERN, request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String headerName = apiKeyProperties.getHeaderName();
        String expectedKey = apiKeyProperties.getCurrentServiceKey();
        String providedKey = request.getHeader(headerName);

        if (expectedKey == null || expectedKey.isBlank() || !expectedKey.equals(providedKey)) {
            writeUnauthorized(response);
            return;
        }

        if (!expectedKey.equals(providedKey)) {
            writeUnauthorized(response);
            return;

        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            null, 
            null, 
            List.of(new SimpleGrantedAuthority(ROLE_INTERNAL))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        ErrorCode errorCode = ErrorCode.INVALID_API_KEY;
        ApiResponse<?> body = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getDefaultMessage())
                .build();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.flushBuffer();
    }
}
