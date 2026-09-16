package com.nongthinh.bo_portal_service.domain.fileconfig;

import java.util.Arrays;
import java.util.Locale;
import com.nongthinh.bo_portal_service.common.exception.ErrorCode;
import com.nongthinh.bo_portal_service.domain.exception.BusinessException;

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

    public String maxSizeParamName() {
        return "FILE_UPLOAD_MAX_BYTES_" + name();
    }

    public static boolean isMaxSizeParam(String name) {
        return Arrays.stream(values()).anyMatch(purpose -> purpose.maxSizeParamName().equals(name));
    }

    public static FilePurpose from(String value) {
        try {
            return valueOf(value == null ? "" : value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.FILE_PURPOSE_INVALID);
        }
    }
}
