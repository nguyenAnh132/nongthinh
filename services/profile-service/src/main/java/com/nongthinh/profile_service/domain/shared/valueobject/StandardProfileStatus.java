package com.nongthinh.profile_service.domain.shared.valueobject;

import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;

public enum StandardProfileStatus {
    ACTIVE,
    LOCKED,
    DELETED,
    DISABLED;

    public static StandardProfileStatus fromString(String raw) {
        if (raw != null) {
            try {
                return StandardProfileStatus.valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.PROFILE_STATUS_INVALID, e);
            }
        }
        throw new BusinessException(ErrorCode.PROFILE_STATUS_INVALID);
    }

    public String getValue() {
        return name().toUpperCase();
    }

    public ErrorCode toAccessDeniedErrorCode() {
        return switch (this) {
            case LOCKED -> ErrorCode.PROFILE_LOCKED;
            case DISABLED -> ErrorCode.PROFILE_DISABLED;
            case DELETED -> ErrorCode.PROFILE_DELETED;
            case ACTIVE -> throw new IllegalStateException("Profile is active");
        };
    }
}
