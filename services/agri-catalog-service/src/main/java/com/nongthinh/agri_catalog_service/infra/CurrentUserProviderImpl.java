package com.nongthinh.agri_catalog_service.infra;

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
import com.nongthinh.agri_catalog_service.common.constant.AdminGroupConstant;
import com.nongthinh.agri_catalog_service.common.constant.ObservabilityConstants;
import com.nongthinh.agri_catalog_service.common.constant.RoleConstant;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;

@Component
public final class CurrentUserProviderImpl implements CurrentUserProvider {

    private static final String EMAIL_CLAIM = "email";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = requireJwt(authentication);
        return new CurrentUser(
                extractUserId(jwt),
                extractKeycloakId(jwt),
                extractEmail(jwt),
                extractRoles(authentication.getAuthorities()),
                extractAdminGroup(authentication.getAuthorities()),
                extractPermissions(authentication.getAuthorities())
        );
    }

    private Jwt requireJwt(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication.getToken();
        }
        throw new BusinessException(ErrorCode.UNAUTHENTICATED);
    }

    private UUID extractUserId(Jwt jwt) {
        String userId = jwt.getClaimAsString(ObservabilityConstants.JWT_USER_ID_CLAIM);
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    private String extractKeycloakId(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        if (keycloakId == null || keycloakId.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return keycloakId;
    }

    private String extractEmail(Jwt jwt) {
        String email = jwt.getClaimAsString(EMAIL_CLAIM);
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return email;
    }

    private Set<String> extractRoles(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(RoleConstant::isApplicationRole)
                .collect(Collectors.toSet());
    }

    private String extractAdminGroup(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(AdminGroupConstant::isAdminGroup)
                .findFirst()
                .orElse(null);
    }

    private Set<String> extractPermissions(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith(ROLE_PREFIX))
                .filter(authority -> !AdminGroupConstant.isAdminGroup(authority))
                .collect(Collectors.toSet());
    }
}
