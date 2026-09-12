package com.nongthinh.profile_service.infra;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import com.nongthinh.profile_service.common.constant.JwtClaimConstants;
import com.nongthinh.profile_service.common.constant.RoleConstant;
import com.nongthinh.profile_service.common.currentuser.CurrentUser;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;

@Component
public final class CurrentUserProviderImpl implements CurrentUserProvider {

    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = requireJwt(authentication);
        UUID userId = extractUserId(jwt);
        String keycloakId = extractKeycloakId(jwt);
        String email = extractEmail(jwt);
        Set<String> roles = extractRoles(authentication.getAuthorities());
        Set<String> permissions = extractPermissions(authentication.getAuthorities());
        return new CurrentUser(userId, keycloakId, email, roles, permissions);
    }

    private Jwt requireJwt(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication.getToken();
        }
        throw new BusinessException(ErrorCode.UNAUTHENTICATED);
    }

    private UUID extractUserId(Jwt jwt) {
        String userId = jwt.getClaimAsString(JwtClaimConstants.USER_ID_CLAIM);
        if (userId == null || userId.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private String extractKeycloakId(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        if (keycloakId == null || keycloakId.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }
        return keycloakId;
    }

    private String extractEmail(Jwt jwt) {
        String email = jwt.getClaimAsString(JwtClaimConstants.EMAIL_CLAIM);
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }
        return email;
    }

    private Set<String> extractRoles(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(RoleConstant::isApplicationRole)
                .collect(Collectors.toSet());
    }

    private Set<String> extractPermissions(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith(ROLE_PREFIX))
                .collect(Collectors.toSet());
    }
}
