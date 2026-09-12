package com.nongthinh.auth_service.application.view;

import java.time.Instant;

public record RefreshTokenView(
    String accessToken,
    String refreshToken,
    Instant expiresIn,
    Instant refreshExpiresIn
) {

}
