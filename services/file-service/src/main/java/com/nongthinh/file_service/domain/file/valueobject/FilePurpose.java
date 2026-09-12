package com.nongthinh.file_service.domain.file.valueobject;

import java.util.Locale;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.domain.exception.BusinessException;

public enum FilePurpose {
    AVATAR,
    BRAND_LOGO,
    BRAND_BANNER,
    BUSINESS_LICENSE,
    PRODUCT_IMAGE,
    DISEASE_IMAGE,
    DIAGNOSIS_IMAGE,
    POST_IMAGE,
    POST_VIDEO,
    MODEL_ARTIFACT;

    public static FilePurpose from(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.PURPOSE_REQUIRED);
        }

        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.PURPOSE_INVALID, ex);
        }
    }
}
