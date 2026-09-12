package com.nongthinh.auth_service.application.port.in.user.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.nongthinh.auth_service.application.command.RegisterAdminCommand;
import com.nongthinh.auth_service.application.event.AdminProfileCreationRequestedEvent;
import com.nongthinh.auth_service.application.port.in.user.RegisterAdminUseCase;
import com.nongthinh.auth_service.application.port.out.ClockProvider;
import com.nongthinh.auth_service.application.port.out.EventPublisher;
import com.nongthinh.auth_service.application.port.out.IdGenerator;
import com.nongthinh.auth_service.application.port.out.PasswordHash;
import com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdp;
import com.nongthinh.auth_service.application.port.out.repository.UserRepository;
import com.nongthinh.auth_service.common.constant.AdminGroupConstant;
import com.nongthinh.auth_service.common.constant.PermissionConstant;
import com.nongthinh.auth_service.common.constant.TokenConstant;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.domain.user.User;
import com.nongthinh.auth_service.domain.user.valueobject.Email;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterAdminUseCaseImpl implements RegisterAdminUseCase {
    private final UserRepository userRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final KeycloakIdp keycloakIdp;
    private final PasswordHash passwordHash;
    private final EventPublisher eventPublisher;

    @Override
    public void execute(RegisterAdminCommand command) {

        long start = System.currentTimeMillis();

        Objects.requireNonNull(command, "command is required");

        if (!AdminGroupConstant.isAdminGroup(command.adminGroup())) {
            throw new BusinessException(ErrorCode.ADMIN_GROUP_INVALID);
        }

        if (userRepository.existsByEmail(command.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Instant now = clockProvider.now();
        UUID userId = idGenerator.generate();

        String clientToken = keycloakIdp.exchangeClientToken();
        String bearerToken = TokenConstant.JWT_TOKEN_PREFIX + clientToken;

        String keycloakUserId = keycloakIdp.createUser(
            userId,
            command.email(),
            command.password(),
            command.temporary(),
            command.enabled(),
            bearerToken
        );

        keycloakIdp.assignRealmRoles(
                keycloakUserId,
                PermissionConstant.realmRolesForAdminGroup(command.adminGroup()),  
                bearerToken
        );

        User newUser = User.createPendingEmailVerification(
            userId,
            keycloakUserId,
            Email.of(command.email()),
            passwordHash.hash(command.email(), command.password()),
            command.enabled(),
            now
        );
        userRepository.save(newUser);//--

        eventPublisher.publish(new AdminProfileCreationRequestedEvent(//--
            idGenerator.generate(),
            now,
            userId,
            command.email(),
            command.firstName(),
            command.lastName(),
            command.phone(),
            command.avatarUrl()
        ));

        log.info(
                "[Application - RegisterAdmin] Admin account created successfully | userId={} adminGroup={} keycloakUserId={} durationMs={}",
                newUser.getId(),
                command.adminGroup(),
                keycloakUserId,
                System.currentTimeMillis() - start
        );
    }
}
