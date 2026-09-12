package com.nongthinh.profile_service.domain.brandprofile.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;

public final class BrandName {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 200;
    private static final Pattern FORMAT = Pattern.compile("^[\\p{L}\\p{N}][\\p{L}\\p{N}\\s&.,\\-'\"()]*$");

    private final String value;

    private BrandName(String value) {
        this.value = value;
    }

    public static BrandName of(String raw) {
        return new BrandName(normalize(raw));
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException(ErrorCode.BRAND_NAME_INVALID);
        }

        String normalized = raw.trim().replaceAll("\\s+", " ");
        if (normalized.length() < MIN_LENGTH || normalized.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.BRAND_NAME_INVALID);
        }
        if (!FORMAT.matcher(normalized).matches()) {
            throw new BusinessException(ErrorCode.BRAND_NAME_INVALID);
        }
        return normalized;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BrandName that)) {
            return false;
        }
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
