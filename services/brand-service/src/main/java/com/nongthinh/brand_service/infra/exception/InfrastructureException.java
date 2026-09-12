package com.nongthinh.brand_service.infra.exception;

import com.nongthinh.brand_service.common.exception.ErrorCode;
import com.nongthinh.brand_service.domain.exception.BusinessException;

public class InfrastructureException extends BusinessException {

    public InfrastructureException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InfrastructureException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public InfrastructureException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
