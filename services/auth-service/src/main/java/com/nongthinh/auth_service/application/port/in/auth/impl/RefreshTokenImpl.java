package com.nongthinh.auth_service.application.port.in.auth.impl;

import org.springframework.stereotype.Service;
import com.nongthinh.auth_service.application.port.in.auth.RefreshToken;
import com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdp;
import com.nongthinh.auth_service.application.view.RefreshTokenView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenImpl implements RefreshToken {

    private final KeycloakIdp keycloakIdp;

    @Override
    public RefreshTokenView execute(String refreshToken) {
        return keycloakIdp.refreshToken(refreshToken);
    }
}
