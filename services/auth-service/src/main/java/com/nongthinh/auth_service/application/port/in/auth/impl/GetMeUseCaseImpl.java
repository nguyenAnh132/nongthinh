package com.nongthinh.auth_service.application.port.in.auth.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.auth_service.application.port.in.auth.GetMeUseCase;
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

    @Override
    public MeView execute(UUID userId, Set<String> roles, String adminGroup, Set<String> permissions) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ProfileView profile = resolveProfile(user.getId(), roles);

        boolean requiresProfileCompletion = requiresProfileCompletion(roles, profile);
        boolean brandRejected = isBrandRejected(profile);

        return new MeView(
                user.getId(),
                user.getEmail().getValue(),
                user.isEnabled(),
                roles.stream().findFirst().orElse(null),
                adminGroup,
                permissions,
                profile,
                new MeFlags(
                        requiresProfileCompletion,
                        brandRejected,
                        brandRejected ? profile.scheduledDeletionAt() : null));
    }

    private ProfileView resolveProfile(UUID userId, Set<String> roles) {
        if (roles.contains(RoleConstant.ROLE_FARMER)) {
            return profileQuery.getFarmerProfile(userId).orElse(null);
        }
        if (roles.contains(RoleConstant.ROLE_BRAND)) {
            return profileQuery.getBrandProfile(userId).orElse(null);
        }
        if (roles.contains(RoleConstant.ROLE_ADMIN)) {
            return profileQuery.getAdminProfile(userId).orElse(null);
        }
        return null;
    }

    private boolean requiresProfileCompletion(Set<String> roles, ProfileView profile) {
        boolean profileRequiredRole = roles.contains(RoleConstant.ROLE_FARMER) || roles.contains(RoleConstant.ROLE_BRAND);
        return profileRequiredRole && profile == null;
    }

    private boolean isBrandRejected(ProfileView profile) {
        return profile != null
                && "BRAND".equals(profile.type())
                && "REJECTED".equals(profile.status());
    }
}
