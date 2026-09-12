package com.nongthinh.profile_service.domain.farmerprofile.valueobject;

import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;

public enum Gender {
    MALE,
    FEMALE,
    OTHER;

    public static Gender fromString(String raw) {
        if (raw != null) {
            try {
                return Gender.valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.GENDER_IS_INVALID, e);
            }
        }
        throw new BusinessException(ErrorCode.GENDER_IS_INVALID);
    }

    public String getValue() {
        return name().toUpperCase();
    }
}
