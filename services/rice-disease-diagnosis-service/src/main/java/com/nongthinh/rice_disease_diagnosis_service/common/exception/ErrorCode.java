package com.nongthinh.rice_disease_diagnosis_service.common.exception;

public enum ErrorCode {
    UNAUTHENTICATED("AUTH_UNAUTHENTICATED", "Unauthenticated", ErrorType.AUTHENTICATION),
    FORBIDDEN("AUTH_FORBIDDEN", "Forbidden", ErrorType.AUTHORIZATION),
    DIAGNOSIS_HISTORY_NOT_FOUND("NOT_FOUND_DIAGNOSIS_HISTORY_NOT_FOUND", "Diagnosis history not found", ErrorType.NOT_FOUND),
    FILE_SERVICE_UNAVAILABLE("INF_FILE_SERVICE_UNAVAILABLE", "File service is unavailable", ErrorType.INFRASTRUCTURE),
    MODEL_REGISTRY_UNAVAILABLE("INF_MODEL_REGISTRY_UNAVAILABLE", "Model registry is unavailable", ErrorType.INFRASTRUCTURE),
    MODEL_NOT_AVAILABLE_FOR_CROP("MODEL_NOT_AVAILABLE_FOR_CROP", "No active model is available for this crop", ErrorType.SERVICE_UNAVAILABLE),
    MODEL_NOT_READY("SYS_MODEL_NOT_READY", "Model runtime is not ready", ErrorType.SERVICE_UNAVAILABLE),
    CATALOG_MAPPING_NOT_READY("SYS_CATALOG_MAPPING_NOT_READY", "Disease catalog mapping is not ready", ErrorType.SERVICE_UNAVAILABLE),
    DIAGNOSIS_CAPACITY_EXCEEDED("SYS_DIAGNOSIS_CAPACITY_EXCEEDED", "Diagnosis capacity is exhausted", ErrorType.SERVICE_UNAVAILABLE),
    INVALID_REQUEST("VAL_INVALID_REQUEST_PARAMETER", "Request parameter or body value is invalid", ErrorType.VALIDATION),
    INTERNAL_ERROR("SYS_INTERNAL_ERROR", "Internal error", ErrorType.SYSTEM);

    private final String code;
    private final String defaultMessage;
    private final ErrorType errorType;

    ErrorCode(String code, String defaultMessage, ErrorType errorType) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.errorType = errorType;
    }

    public String getCode() { return code; }
    public String getDefaultMessage() { return defaultMessage; }
    public ErrorType getErrorType() { return errorType; }
}
