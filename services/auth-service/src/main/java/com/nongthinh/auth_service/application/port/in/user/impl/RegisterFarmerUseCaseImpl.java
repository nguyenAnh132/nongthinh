package com.nongthinh.auth_service.application.port.in.user.impl;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.nongthinh.auth_service.infra.exception.InfrastructureException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.nongthinh.auth_service.application.command.RegisterFarmerCommand;
import com.nongthinh.auth_service.application.event.FarmerProfileCreationRequestedEvent;
import com.nongthinh.auth_service.application.port.in.user.RegisterFarmerUseCase;
import com.nongthinh.auth_service.application.port.out.ClockProvider;
import com.nongthinh.auth_service.application.port.out.EventPublisher;
import com.nongthinh.auth_service.application.port.out.IdGenerator;
import com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdp;
import com.nongthinh.auth_service.application.port.out.keycloak.RoleRecord;
import com.nongthinh.auth_service.application.port.out.repository.UserRepository;
import com.nongthinh.auth_service.common.constant.RoleConstant;
import com.nongthinh.auth_service.common.constant.TokenConstant;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.domain.user.User;
import com.nongthinh.auth_service.domain.user.valueobject.Email;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterFarmerUseCaseImpl implements RegisterFarmerUseCase {

    private final UserRepository userRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final KeycloakIdp keycloakIdp;
    private final EventPublisher eventPublisher;

    @Override
    public void execute(RegisterFarmerCommand command) {
        long start = System.currentTimeMillis();
        Objects.requireNonNull(command, "command is required");

        if (userRepository.existsByEmail(command.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        Instant now = clockProvider.now();
        UUID userId = idGenerator.generate();

        String clientToken = keycloakIdp.exchangeClientToken();

        String keycloakUserId = keycloakIdp.createUser(
                userId,
                command.email(),
                command.password(),
                command.temporary(),
                command.enabled(),
                TokenConstant.JWT_TOKEN_PREFIX + clientToken
        );


        RoleRecord role = keycloakIdp.getRoleByName(RoleConstant.ROLE_FARMER, TokenConstant.JWT_TOKEN_PREFIX + clientToken);
        keycloakIdp.assignRealmRoles(keycloakUserId, List.of(role.name()), TokenConstant.JWT_TOKEN_PREFIX + clientToken);

        User newUser = User.create(
            userId,
            keycloakUserId,
            Email.of(command.email()),
            now
        );
        userRepository.save(newUser);

        eventPublisher.publish(new FarmerProfileCreationRequestedEvent(
            idGenerator.generate(),
            now,
            userId,
            command.email(),
            command.firstName(),
            command.lastName(),
            command.gender(),
            command.phone(),
            command.provinceId(),
            command.communeId(),
            command.addressDetail(),
            command.avatarUrl()
        ));

        log.info(
                "[Application - RegisterFarmer] Farmer account created successfully | userId={} role={} keycloakUserId={} durationMs={}",
                newUser.getId(),
                role.name(),
                keycloakUserId,
                System.currentTimeMillis() - start
        );
    }
}

