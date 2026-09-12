package com.nongthinh.notification_service.infra.exception;

import com.nongthinh.notification_service.common.exception.AppException;
import com.nongthinh.notification_service.common.exception.ErrorCode;

public class InfrastructureException extends AppException {

    public InfrastructureException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public InfrastructureException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InfrastructureException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public InfrastructureException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}
