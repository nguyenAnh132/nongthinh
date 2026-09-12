package com.nongthinh.auth_service.infra.caching.otp;

public final class OtpCacheKeys {

    private static final String PREFIX = "auth:otp:";

    private OtpCacheKeys() {
    }

    public static String forEmail(String email) {
        return PREFIX + email;
    }
}
