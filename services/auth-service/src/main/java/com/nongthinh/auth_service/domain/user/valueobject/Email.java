package com.nongthinh.auth_service.domain.user.valueobject;

import java.util.Locale;
import java.util.regex.Pattern;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;

public final class Email {

    private static final Pattern EMAIL_FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String value;

    private Email (String value) {
        this.value = value;
    }

    public static Email of (String raw) {
        return new Email(normalize(raw));
    }

    public static String normalize(String raw) {
        if(raw == null || raw.isBlank()) {
            throw new BusinessException(ErrorCode.EMAIL_INVALID);
        }

        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (!(EMAIL_FORMAT.matcher(normalized).matches())) {
            throw new BusinessException(ErrorCode.EMAIL_FORMAT_INVALID);
        }
        return normalized;
    }

    public String getValue() {
        return value;
    }
}
