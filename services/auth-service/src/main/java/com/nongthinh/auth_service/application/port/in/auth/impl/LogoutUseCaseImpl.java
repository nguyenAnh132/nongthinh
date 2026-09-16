package com.nongthinh.auth_service.application.port.in.auth.impl;

import org.springframework.stereotype.Service;
import com.nongthinh.auth_service.application.port.in.auth.LogoutUseCase;
import com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdp;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final KeycloakIdp keycloakIdp;

    @Override
    public void execute(String refreshToken) {
        keycloakIdp.logout(refreshToken);
    }
}
