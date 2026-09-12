package com.nongthinh.auth_service.common.cookie;

import java.time.Instant;

public record TokenCookie(
    String accessToken,
    String refreshToken,
    Instant expiresIn,
    Instant refreshExpiresIn
) {

}
