package com.nongthinh.auth_service.infra.exception;

import com.nongthinh.auth_service.common.exception.AppException;
import com.nongthinh.auth_service.common.exception.ErrorCode;

public class InfrastructureException extends AppException {

    public InfrastructureException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public InfrastructureException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public InfrastructureException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public InfrastructureException(ErrorCode errorCode) {
        super(errorCode);
    }
}
