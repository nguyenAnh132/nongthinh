package com.nongthinh.auth_service.infra;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import com.nongthinh.auth_service.common.constant.AdminGroupConstant;
import com.nongthinh.auth_service.common.constant.ObservabilityConstants;
import com.nongthinh.auth_service.common.constant.PermissionConstant;
import com.nongthinh.auth_service.common.constant.RoleConstant;
import com.nongthinh.auth_service.common.currentuser.CurrentUser;
import com.nongthinh.auth_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
@RequiredArgsConstructor
public final class CurrentUserProviderImpl implements CurrentUserProvider {

    private static final String EMAIL_CLAIM = "email";

    @Override
    public com.nongthinh.auth_service.application.view.RegistrationPrincipal getRegistrationPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = requireJwt(authentication);
        String claim = jwt.getClaimAsString(ObservabilityConstants.JWT_USER_ID_CLAIM);
        try {
            return new com.nongthinh.auth_service.application.view.RegistrationPrincipal(
                    UUID.fromString(extractKeycloakId(jwt)), extractEmail(jwt),
                    claim == null || claim.isBlank() ? null : UUID.fromString(claim),
                    extractPrimaryRole(authentication.getAuthorities()),
                    extractAdminGroup(authentication.getAuthorities()), extractPermissions(authentication.getAuthorities()));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    @Override
    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = requireJwt(authentication);
        String userId = extractUserId(jwt);
        String keycloakId = extractKeycloakId(jwt);
        String email = extractEmail(jwt);
        String role = extractPrimaryRole(authentication.getAuthorities());
        String adminGroup = extractAdminGroup(authentication.getAuthorities());
        Set<String> permissions = extractPermissions(authentication.getAuthorities());
        return new CurrentUser(
                UUID.fromString(userId),
                keycloakId,
                email,
                Set.of(role),
                adminGroup,
                permissions
        );
    }

    private Jwt requireJwt(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication.getToken();
        }
        throw new BusinessException(ErrorCode.UNAUTHENTICATED);
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

    private String extractUserId(Jwt jwt) {
        String userId = jwt.getClaimAsString(ObservabilityConstants.JWT_USER_ID_CLAIM);
        if (userId == null || userId.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return userId;
    }

    private String extractPrimaryRole(Collection<? extends GrantedAuthority> authorities) {
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(RoleConstant::isApplicationRole)
                .toList();

        if (roles.contains(RoleConstant.ROLE_ADMIN)) {
            return RoleConstant.ROLE_ADMIN;
        }
        if (roles.contains(RoleConstant.ROLE_BRAND)) {
            return RoleConstant.ROLE_BRAND;
        }
        if (roles.contains(RoleConstant.ROLE_BRAND_PENDING)) {
            return RoleConstant.ROLE_BRAND_PENDING;
        }
        if (roles.contains(RoleConstant.ROLE_FARMER)) {
            return RoleConstant.ROLE_FARMER;
        }
        throw new BusinessException(ErrorCode.ROLES_REQUIRED);
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
                .filter(PermissionConstant::isPermission)
                .distinct()
                .sorted()
                .collect(Collectors.toSet());
    }
}
