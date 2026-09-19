package com.nongthinh.auth_service.infra.client.profileservice;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.nongthinh.auth_service.application.command.CompleteRegistrationCommand;
import com.nongthinh.auth_service.application.port.out.ProfileRegistration;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.infra.client.profileservice.dto.CompleteProfileRegistrationParam;
import com.nongthinh.auth_service.infra.exception.InfrastructureException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProfileRegistrationImpl implements ProfileRegistration {
    private final ProfileClient client;
    private final String apiKey;

    public ProfileRegistrationImpl(ProfileClient client,
            @Value("${spring.security.api-key.clients.profile-service.api-key}") String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
    }

    @Override
    public void complete(UUID userId, String role, CompleteRegistrationCommand command) {
        try {
            client.completeRegistration(new CompleteProfileRegistrationParam(userId, role, command.firstName(),
                    command.lastName(), command.gender(), command.phone(), command.brandName(),
                    command.representativeName(), command.representativePhone(), command.representativeEmail()), apiKey);
        } catch (FeignException ex) {
            log.warn("[Infra - ProfileRegistration] Completion failed | userId={} role={} upstreamStatus={}",
                    userId, role, ex.status());
            throw new InfrastructureException(ErrorCode.PROFILE_REGISTRATION_FAILED);
        }
    }
}
