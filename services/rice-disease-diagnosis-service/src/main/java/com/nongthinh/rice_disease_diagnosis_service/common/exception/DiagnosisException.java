package com.nongthinh.rice_disease_diagnosis_service.common.exception;

public class DiagnosisException extends RuntimeException {
    private final ErrorCode errorCode;

    public DiagnosisException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public DiagnosisException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
