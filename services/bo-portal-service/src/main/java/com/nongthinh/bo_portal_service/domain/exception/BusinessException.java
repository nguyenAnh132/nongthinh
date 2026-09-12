package com.nongthinh.bo_portal_service.domain.exception;

import com.nongthinh.bo_portal_service.common.exception.AppException;
import com.nongthinh.bo_portal_service.common.exception.ErrorCode;

public class BusinessException extends AppException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
