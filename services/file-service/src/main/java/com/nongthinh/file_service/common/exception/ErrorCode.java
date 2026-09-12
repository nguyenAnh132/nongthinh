package com.nongthinh.file_service.common.exception;

public enum ErrorCode {

    FILE_NOT_FOUND("FIL_FILE_NOT_FOUND", "File not found", ErrorType.NOT_FOUND),
    FILE_ALREADY_DELETED("FIL_FILE_ALREADY_DELETED", "File has already been deleted", ErrorType.BUSINESS_RULE),
    FILE_ACCESS_DENIED("FIL_FILE_ACCESS_DENIED", "You do not have access to this file", ErrorType.AUTHORIZATION),
    FILE_TOO_LARGE("FIL_FILE_TOO_LARGE", "File size exceeds the allowed limit", ErrorType.VALIDATION),
    CONTENT_TYPE_NOT_ALLOWED("FIL_CONTENT_TYPE_NOT_ALLOWED", "Content type is not allowed for this file purpose",
            ErrorType.VALIDATION),
    FILE_PURPOSE_NOT_ALLOWED("FIL_FILE_PURPOSE_NOT_ALLOWED", "You are not allowed to upload files for this purpose",
            ErrorType.AUTHORIZATION),
    FILE_NAME_NOT_ALLOWED("FIL_FILE_NAME_NOT_ALLOWED", "File name is not allowed for this file purpose",
            ErrorType.VALIDATION),
    FILE_MANAGED_LIFECYCLE("FIL_FILE_MANAGED_LIFECYCLE", "This file must be deleted by its owning workflow",
            ErrorType.BUSINESS_RULE),
    FILE_PURPOSE_MISMATCH("FIL_FILE_PURPOSE_MISMATCH", "File purpose does not match this operation",
            ErrorType.VALIDATION),
    FILE_EMPTY("FIL_FILE_EMPTY", "Uploaded file is empty", ErrorType.VALIDATION),
    STORAGE_UNAVAILABLE("FIL_STORAGE_UNAVAILABLE", "Object storage is unavailable", ErrorType.INTERNAL_SERVICE),
    PURPOSE_REQUIRED("FIL_PURPOSE_REQUIRED", "File purpose is required", ErrorType.VALIDATION),
    PURPOSE_INVALID("FIL_PURPOSE_INVALID", "File purpose is invalid", ErrorType.VALIDATION),

    VALIDATION_FAILED("FIL_VALIDATION_FAILED", "Validation failed", ErrorType.VALIDATION),
    INTERNAL_ERROR("FIL_INTERNAL_ERROR", "Internal server error", ErrorType.SYSTEM),

    UNAUTHENTICATED("UNAUTHENTICATED", "Unauthenticated", ErrorType.AUTHENTICATION),
    FORBIDDEN("FORBIDDEN", "Forbidden", ErrorType.AUTHORIZATION),
    INVALID_API_KEY("INVALID_API_KEY", "Invalid API key", ErrorType.AUTHENTICATION),
    INVALID_KEY("INVALID_KEY", "Invalid key", ErrorType.VALIDATION),
    ;

    private final String code;
    private final String defaultMessage;
    private final ErrorType errorType;

    ErrorCode(String code, String message, ErrorType errorType) {
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
