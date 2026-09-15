package com.nongthinh.auth_service.application.port.in.user.impl;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.nongthinh.auth_service.application.command.RegisterBrandCommand;
import com.nongthinh.auth_service.application.event.BrandProfileCreationRequestedEvent;
import com.nongthinh.auth_service.application.port.in.user.RegisterBrandUseCase;
import com.nongthinh.auth_service.application.port.out.ClockProvider;
import com.nongthinh.auth_service.application.port.out.EventPublisher;
import com.nongthinh.auth_service.application.port.out.IdGenerator;
import com.nongthinh.auth_service.application.port.out.PasswordHash;
import com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdp;
import com.nongthinh.auth_service.application.port.out.repository.UserRepository;
import com.nongthinh.auth_service.common.constant.TokenConstant;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.domain.user.User;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import com.nongthinh.auth_service.common.constant.RoleConstant;
import com.nongthinh.auth_service.application.port.out.keycloak.RoleRecord;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterBrandUseCaseImpl implements RegisterBrandUseCase {
    private final UserRepository userRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final KeycloakIdp keycloakIdp;
    private final PasswordHash passwordHash;
    private final EventPublisher eventPublisher;

    @Override
    public void execute(RegisterBrandCommand command) {
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

        RoleRecord role = keycloakIdp.getRoleByName(RoleConstant.ROLE_BRAND, TokenConstant.JWT_TOKEN_PREFIX + clientToken);
        keycloakIdp.assignRealmRoles(keycloakUserId, List.of(role.name()), TokenConstant.JWT_TOKEN_PREFIX + clientToken);

        User newUser = User.create(
            userId,
            keycloakUserId,
            Email.of(command.email()),
            passwordHash.hash(command.email(), command.password()),
            command.enabled(),
            now
        );
        userRepository.save(newUser);

        eventPublisher.publish(new BrandProfileCreationRequestedEvent(
            idGenerator.generate(),
            now,
            userId,
            command.email(),
            command.brandName(),
            command.taxCode(),
            command.description(),
            command.phone(),
            command.officeProvinceId(),
            command.officeCommuneId(),
            command.officeAddressDetail(),
            command.representativeName(),
            command.representativePhone(),
            command.representativeEmail(),
            command.logoUrl(),
            command.bannerUrl(),
            command.websiteUrl()
        ));

        log.info(
                "[Application - RegisterBrand] Brand account created successfully | userId={} role={} keycloakUserId={} durationMs={}",
                newUser.getId(),
                role.name(),
                keycloakUserId,
                System.currentTimeMillis() - start
        );
    }
}
