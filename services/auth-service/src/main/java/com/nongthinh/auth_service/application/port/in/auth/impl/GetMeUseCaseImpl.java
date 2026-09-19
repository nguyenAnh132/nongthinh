package com.nongthinh.auth_service.application.port.in.auth.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.auth_service.application.port.in.auth.GetMeUseCase;
import com.nongthinh.auth_service.application.exception.RegistrationRequiredException;
import com.nongthinh.auth_service.application.view.RegistrationPrincipal;
import com.nongthinh.auth_service.application.port.out.ProfileQuery;
import com.nongthinh.auth_service.application.port.out.repository.UserRepository;
import com.nongthinh.auth_service.application.view.MeFlags;
import com.nongthinh.auth_service.application.view.MeView;
import com.nongthinh.auth_service.application.view.ProfileView;
import com.nongthinh.auth_service.common.constant.RoleConstant;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.domain.user.User;
import lombok.RequiredArgsConstructor;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GetMeUseCaseImpl implements GetMeUseCase {

    private final UserRepository userRepository;
    private final ProfileQuery profileQuery;
    private final com.nongthinh.auth_service.application.port.in.user.SynchronizeBrandRoleUseCase synchronizeBrandRoleUseCase;

    @Override
    public MeView execute(RegistrationPrincipal principal) {
        var user = userRepository.findByKeycloakId(principal.keycloakId());
        if (user.isEmpty()) {
            if (principal.applicationUserId() != null && userRepository.findById(principal.applicationUserId()).isPresent()) {
                throw new BusinessException(ErrorCode.REGISTRATION_IDENTITY_CONFLICT);
            }
            throw new RegistrationRequiredException(principal.email(), principal.role());
        }
        if (user.get().getDeletedAt() != null) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (principal.applicationUserId() == null) {
            throw new RegistrationRequiredException(principal.email(), principal.role());
        }
        if (!user.get().getId().equals(principal.applicationUserId())) {
            throw new BusinessException(ErrorCode.REGISTRATION_IDENTITY_CONFLICT);
        }
        return execute(user.get().getId(), Set.of(principal.role()), principal.adminGroup(), principal.permissions());
    }

    @Override
    public MeView execute(UUID userId, Set<String> roles, String adminGroup, Set<String> permissions) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ProfileView profile = resolveProfile(user.getId(), roles);

        if (user.getDeletedAt() != null) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (profile == null) {
            throw new RegistrationRequiredException(
                    user.getEmail().getValue(), roles.stream().findFirst().orElseThrow());
        }
        if (Set.of("LOCKED", "DISABLED", "DELETED").contains(profile.status())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        boolean brandAccount = roles.contains(RoleConstant.ROLE_BRAND) || roles.contains(RoleConstant.ROLE_BRAND_PENDING);
        boolean activeBrand = "ACTIVE".equals(profile.status());
        if (brandAccount && activeBrand != roles.contains(RoleConstant.ROLE_BRAND)) {
            synchronizeBrandRoleUseCase.execute(userId);
        }

        boolean brandRejected = isBrandRejected(profile);

        return new MeView(
                user.getId(),
                user.getEmail().getValue(),
                roles.stream().findFirst().orElse(null),
                adminGroup,
                permissions,
                profile,
                new MeFlags(
                        false,
                        brandRejected,
                        brandRejected ? profile.scheduledDeletionAt() : null));
    }

    private ProfileView resolveProfile(UUID userId, Set<String> roles) {
        if (roles.contains(RoleConstant.ROLE_FARMER)) {
            return profileQuery.getFarmerProfile(userId).orElse(null);
        }
        if ((roles.contains(RoleConstant.ROLE_BRAND) || roles.contains(RoleConstant.ROLE_BRAND_PENDING))) {
            return profileQuery.getBrandProfile(userId).orElse(null);
        }
        if (roles.contains(RoleConstant.ROLE_ADMIN)) {
            return profileQuery.getAdminProfile(userId).orElse(null);
        }
        return null;
    }

    private boolean isBrandRejected(ProfileView profile) {
        return profile != null
                && "BRAND".equals(profile.type())
                && "REJECTED".equals(profile.status());
    }
}
