package com.nongthinh.auth_service.application.port.in.user.impl;

import java.util.List;
import java.util.UUID;
import com.nongthinh.auth_service.application.port.in.user.SynchronizeBrandRoleUseCase;
import com.nongthinh.auth_service.application.port.out.ProfileQuery;
import com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdp;
import com.nongthinh.auth_service.application.port.out.repository.UserRepository;
import com.nongthinh.auth_service.application.view.ProfileView;
import com.nongthinh.auth_service.common.constant.RoleConstant;
import com.nongthinh.auth_service.common.constant.TokenConstant;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SynchronizeBrandRoleUseCaseImpl implements SynchronizeBrandRoleUseCase {
    private final UserRepository userRepository;
    private final ProfileQuery profileQuery;
    private final KeycloakIdp keycloakIdp;

    @Override
    public void execute(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        // Read the authoritative state instead of trusting a delayed or duplicate event's status.
        boolean active = profileQuery.getBrandProfile(userId).map(ProfileView::status)
                .filter("ACTIVE"::equals).isPresent();
        String desired = active ? RoleConstant.ROLE_BRAND : RoleConstant.ROLE_BRAND_PENDING;
        String obsolete = active ? RoleConstant.ROLE_BRAND_PENDING : RoleConstant.ROLE_BRAND;
        String token = TokenConstant.JWT_TOKEN_PREFIX + keycloakIdp.exchangeClientToken();
        var currentRoles = keycloakIdp.getRealmRoleNames(user.getKeycloakId(), token);
        // Grant the restricted role first so a failed revocation never leaves the user unable to view their status.
        if (!currentRoles.contains(desired)) {
            keycloakIdp.assignRealmRoles(user.getKeycloakId(), List.of(desired), token);
        }
        if (currentRoles.contains(obsolete)) {
            keycloakIdp.removeRealmRoles(user.getKeycloakId(), List.of(obsolete), token);
        }
    }
}
