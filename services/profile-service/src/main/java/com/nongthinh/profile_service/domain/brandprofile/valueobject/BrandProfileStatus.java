package com.nongthinh.profile_service.domain.brandprofile.valueobject;

import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;

public enum BrandProfileStatus {
    UNDER_REVIEW,   
    NEEDS_REVISION,
    READY_FOR_FINAL_REVIEW,
    PENDING_APPROVAL,
    ACTIVE,
    REJECTED,
    DELETED,
    LOCKED,
    DISABLED;

    public static BrandProfileStatus fromString(String raw) {
        if (raw != null) {
            try {
                return BrandProfileStatus.valueOf(raw.trim().toUpperCase());
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
            case UNDER_REVIEW -> ErrorCode.PROFILE_UNDER_REVIEW;
            case NEEDS_REVISION -> ErrorCode.PROFILE_NEEDS_REVISION;
            case READY_FOR_FINAL_REVIEW -> ErrorCode.PROFILE_READY_FOR_FINAL_REVIEW;
            case PENDING_APPROVAL -> ErrorCode.PROFILE_PENDING_APPROVAL;
            case REJECTED -> ErrorCode.PROFILE_REJECTED;
            case LOCKED -> ErrorCode.PROFILE_LOCKED;
            case DISABLED -> ErrorCode.PROFILE_DISABLED;
            case DELETED -> ErrorCode.PROFILE_DELETED;
            case ACTIVE -> throw new IllegalStateException("Profile is active");
        };
    }
}
