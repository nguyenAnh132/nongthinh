package com.nongthinh.bo_portal_service.common.exception;

public enum ErrorCode {
    BRAND_ACCESS_DENIED("BRAND_ACCESS_DENIED", "Brand verification is required for this operation", ErrorType.AUTHORIZATION),
    BRAND_ACCESS_UNAVAILABLE("BRAND_ACCESS_UNAVAILABLE", "Cannot verify brand access", ErrorType.INFRASTRUCTURE),

    FILE_PURPOSE_INVALID("FILE_PURPOSE_INVALID", "File purpose is invalid", ErrorType.VALIDATION),
    FILE_PURPOSE_TYPE_NOT_FOUND("FILE_PURPOSE_TYPE_NOT_FOUND", "File type is not supported for this purpose", ErrorType.NOT_FOUND),
    FILE_TYPE_ENABLED_REQUIRED("FILE_TYPE_ENABLED_REQUIRED", "File type enabled flag is required", ErrorType.VALIDATION),

    UNAUTHENTICATED("UNAUTHENTICATED", "Unauthenticated", ErrorType.AUTHENTICATION),
    INVALID_API_KEY("INVALID_API_KEY", "Invalid API key", ErrorType.AUTHENTICATION),
    FORBIDDEN("FORBIDDEN", "Forbidden", ErrorType.AUTHORIZATION),
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal error", ErrorType.SYSTEM),
    INVALID_KEY("INVALID_KEY", "Invalid key", ErrorType.VALIDATION),

    SYSTEM_PARAM_NOT_FOUND("SYSTEM_PARAM_NOT_FOUND", "System param not found", ErrorType.NOT_FOUND),
    SYSTEM_PARAM_NAME_DUPLICATED("SYSTEM_PARAM_NAME_DUPLICATED", "System param name already exists", ErrorType.BUSINESS_RULE),
    SYSTEM_PARAM_CANNOT_DELETE("SYSTEM_PARAM_CANNOT_DELETE", "System-defined param cannot be deleted", ErrorType.BUSINESS_RULE),
    SYSTEM_PARAM_VALUE_INVALID("SYSTEM_PARAM_VALUE_INVALID", "System param value is invalid for its data type", ErrorType.VALIDATION),
    SYSTEM_PARAM_NAME_REQUIRED("SYSTEM_PARAM_NAME_REQUIRED", "System param name is required", ErrorType.VALIDATION),
    SYSTEM_PARAM_VALUE_REQUIRED("SYSTEM_PARAM_VALUE_REQUIRED", "System param value is required", ErrorType.VALIDATION),
    SYSTEM_PARAM_DATA_TYPE_INVALID("SYSTEM_PARAM_DATA_TYPE_INVALID", "System param data type is invalid", ErrorType.VALIDATION),

    SYSTEM_PARAM_TYPE_NOT_FOUND("SYSTEM_PARAM_TYPE_NOT_FOUND", "System param type not found", ErrorType.NOT_FOUND),
    SYSTEM_PARAM_TYPE_NAME_DUPLICATED("SYSTEM_PARAM_TYPE_NAME_DUPLICATED", "System param type name already exists", ErrorType.BUSINESS_RULE),
    SYSTEM_PARAM_TYPE_NAME_REQUIRED("SYSTEM_PARAM_TYPE_NAME_REQUIRED", "System param type name is required", ErrorType.VALIDATION),
    SYSTEM_PARAM_TYPE_ID_REQUIRED("SYSTEM_PARAM_TYPE_ID_REQUIRED", "System param type id is required", ErrorType.VALIDATION),
    ;

    private final String code;
    private final String defaultMessage;
    private final ErrorType errorType;

    private ErrorCode(String code, String message, ErrorType errorType) {
        this.code = code;
        this.defaultMessage = message;
        this.errorType = errorType;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
