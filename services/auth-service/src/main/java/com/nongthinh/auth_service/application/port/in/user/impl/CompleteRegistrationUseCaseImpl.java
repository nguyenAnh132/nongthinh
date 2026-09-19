package com.nongthinh.auth_service.application.port.in.user.impl;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import com.nongthinh.auth_service.application.command.CompleteRegistrationCommand;
import com.nongthinh.auth_service.application.port.in.user.CompleteRegistrationUseCase;
import com.nongthinh.auth_service.application.port.in.user.SynchronizeBrandRoleUseCase;
import com.nongthinh.auth_service.application.port.out.ClockProvider;
import com.nongthinh.auth_service.application.port.out.IdGenerator;
import com.nongthinh.auth_service.application.port.out.ProfileRegistration;
import com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdp;
import com.nongthinh.auth_service.application.port.out.repository.UserRepository;
import com.nongthinh.auth_service.application.view.CompleteRegistrationView;
import com.nongthinh.auth_service.application.view.RegistrationPrincipal;
import com.nongthinh.auth_service.common.constant.RoleConstant;
import com.nongthinh.auth_service.common.constant.TokenConstant;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.domain.user.User;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompleteRegistrationUseCaseImpl implements CompleteRegistrationUseCase {
    private final UserRepository users;
    private final KeycloakIdp keycloak;
    private final ProfileRegistration profiles;
    private final SynchronizeBrandRoleUseCase synchronizeBrandRole;
    private final ClockProvider clock;
    private final IdGenerator ids;

    @Override
    public CompleteRegistrationView execute(RegistrationPrincipal principal, CompleteRegistrationCommand command) {
        Objects.requireNonNull(principal, "principal is required");
        Objects.requireNonNull(command, "command is required").validateFor(principal.role());
        String token = TokenConstant.JWT_TOKEN_PREFIX + keycloak.exchangeClientToken();
        var external = keycloak.getIdentity(principal.keycloakId(), token);
        if (!external.enabled()) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (!principal.keycloakId().equals(external.id())
                || !Email.normalize(principal.email()).equals(Email.normalize(external.email()))
                || (principal.applicationUserId() != null && !principal.applicationUserId().equals(external.applicationUserId()))) {
            throw new BusinessException(ErrorCode.REGISTRATION_IDENTITY_CONFLICT);
        }
        var existing = users.findByKeycloakId(principal.keycloakId());
        if (existing.isEmpty()) {
            var userId = external.applicationUserId() == null ? ids.generate() : external.applicationUserId();
            users.insertIfAbsent(User.create(userId, principal.keycloakId().toString(), Email.of(external.email()), clock.now()));
        }
        User user = users.findByKeycloakId(principal.keycloakId())
                .orElseThrow(() -> new BusinessException(ErrorCode.REGISTRATION_IDENTITY_CONFLICT));
        if (user.getDeletedAt() != null) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (!user.getEmail().getValue().equals(Email.normalize(external.email()))
                || (external.applicationUserId() != null && !user.getId().equals(external.applicationUserId()))) {
            throw new BusinessException(ErrorCode.REGISTRATION_IDENTITY_CONFLICT);
        }
        keycloak.updateNongThinhIdUser(UUID.fromString(user.getKeycloakId()), user.getId(), token);
        profiles.complete(user.getId(), principal.role(), command);
        if (RoleConstant.ROLE_BRAND.equals(principal.role()) || RoleConstant.ROLE_BRAND_PENDING.equals(principal.role())) {
            synchronizeBrandRole.execute(user.getId());
        }
        return new CompleteRegistrationView(user.getId(), true);
    }
}
