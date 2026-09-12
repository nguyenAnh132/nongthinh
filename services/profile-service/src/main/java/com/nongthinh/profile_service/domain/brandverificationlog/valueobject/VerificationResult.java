package com.nongthinh.profile_service.domain.brandverificationlog.valueobject;

import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;

public enum VerificationResult {
    VERIFIED,
    UNREACHABLE,
    NEED_MORE_INFO;

    public static VerificationResult fromString(String raw) {
        if (raw != null) {
            try {
                return VerificationResult.valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.VERIFICATION_RESULT_INVALID, e);
            }
        }
        throw new BusinessException(ErrorCode.VERIFICATION_RESULT_INVALID);
    }

    public String getValue() {
        return name();
    }
}
