package com.nongthinh.post_service.domain.exception;

import com.nongthinh.post_service.common.exception.AppException;
import com.nongthinh.post_service.common.exception.ErrorCode;

public class BusinessException extends AppException {
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
