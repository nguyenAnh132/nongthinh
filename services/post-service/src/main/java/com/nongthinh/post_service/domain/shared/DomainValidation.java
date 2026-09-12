package com.nongthinh.post_service.domain.shared;

import java.time.Instant;
import java.util.UUID;

public final class DomainValidation {
    private DomainValidation() {
    }

    public static <T> T required(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    public static UUID requiredId(UUID value, String field) {
        return required(value, field);
    }

    public static String requiredText(String value, int maxLength, String field) {
        String normalized = normalizeText(value, field);
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(field + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    public static String optionalText(String value, int maxLength, String field) {
        return value == null ? null : requiredText(value, maxLength, field);
    }

    public static Instant notBefore(Instant value, Instant reference, String field, String referenceField) {
        Instant requiredValue = required(value, field);
        Instant requiredReference = required(reference, referenceField);
        if (requiredValue.isBefore(requiredReference)) {
            throw new IllegalArgumentException(field + " must not be before " + referenceField);
        }
        return requiredValue;
    }

    private static String normalizeText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
