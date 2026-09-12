package com.nongthinh.bo_portal_service.domain.systemparam;

import com.nongthinh.bo_portal_service.common.exception.ErrorCode;
import com.nongthinh.bo_portal_service.domain.exception.BusinessException;

public enum SystemParamDataType {

    STRING,
    INTEGER,
    BOOLEAN;

    public static SystemParamDataType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_DATA_TYPE_INVALID);
        }
        try {
            return SystemParamDataType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_DATA_TYPE_INVALID, "Unsupported data type: " + raw);
        }
    }

    public void validate(String value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_VALUE_REQUIRED);
        }
        switch (this) {
            case INTEGER -> {
                try {
                    Integer.parseInt(value.trim());
                } catch (NumberFormatException ex) {
                    throw new BusinessException(ErrorCode.SYSTEM_PARAM_VALUE_INVALID, "Value must be an integer: " + value);
                }
            }
            case BOOLEAN -> {
                String normalized = value.trim().toLowerCase();
                if (!"true".equals(normalized) && !"false".equals(normalized)) {
                    throw new BusinessException(ErrorCode.SYSTEM_PARAM_VALUE_INVALID, "Value must be 'true' or 'false': " + value);
                }
            }
            case STRING -> {
                if (value.isBlank()) {
                    throw new BusinessException(ErrorCode.SYSTEM_PARAM_VALUE_REQUIRED);
                }
            }
        }
    }
}
