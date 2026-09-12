package com.nongthinh.auth_service.domain.user.valueobject;

public enum AuthProvider {

    LOCAL,
    GOOGLE,
    FACEBOOK;

    public static AuthProvider fromDbValue(String value) {
        if (value == null || value.isBlank()) {
            return LOCAL;
        }

        return AuthProvider.valueOf(value.trim().toUpperCase());
    }
}
