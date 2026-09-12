package com.nongthinh.profile_service.domain.brandlifecyclelog.valueobject;

import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;

public enum BrandLifecycleAction {
    STATUS_CHANGED,
    DOCUMENT_UPLOADED,
    DOCUMENTS_SUBMITTED,
    DOCUMENTS_REQUESTED,
    DOCUMENT_REVIEWED,
    VERIFICATION_RECORDED,
    EARLY_REJECTED;

    public static BrandLifecycleAction fromString(String raw) {
        if (raw != null) {
            try {
                return BrandLifecycleAction.valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.BRAND_LIFECYCLE_ACTION_INVALID, e);
            }
        }
        throw new BusinessException(ErrorCode.BRAND_LIFECYCLE_ACTION_INVALID);
    }

    public String getValue() {
        return name();
    }
}
