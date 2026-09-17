package com.nongthinh.location_service.common.exception;

public enum ErrorCode {
    BRAND_ACCESS_DENIED("BRAND_ACCESS_DENIED", "Brand verification is required for this operation", ErrorType.AUTHORIZATION),
    BRAND_ACCESS_UNAVAILABLE("BRAND_ACCESS_UNAVAILABLE", "Cannot verify brand access", ErrorType.INTERNAL_SERVICE),

    PROVINCE_NOT_FOUND("LOC_PROVINCE_NOT_FOUND", "Province not found", ErrorType.NOT_FOUND),
    COMMUNE_NOT_FOUND("LOC_COMMUNE_NOT_FOUND", "Commune not found", ErrorType.NOT_FOUND),

    PROVINCE_ALREADY_EXISTS("LOC_PROVINCE_ALREADY_EXISTS", "Province already exists", ErrorType.BUSINESS_RULE),
    PROVINCE_CODE_ALREADY_EXISTS("LOC_PROVINCE_CODE_ALREADY_EXISTS", "Province code already exists",
            ErrorType.BUSINESS_RULE),
    PROVINCE_HAS_COMMUNES("LOC_PROVINCE_HAS_COMMUNES", "Province cannot be deleted because it still has communes",
            ErrorType.BUSINESS_RULE),

    COMMUNE_ALREADY_EXISTS("LOC_COMMUNE_ALREADY_EXISTS", "Commune already exists", ErrorType.BUSINESS_RULE),
    COMMUNE_CODE_ALREADY_EXISTS("LOC_COMMUNE_CODE_ALREADY_EXISTS", "Commune code already exists",
            ErrorType.BUSINESS_RULE),

    VALIDATION_FAILED("LOC_VALIDATION_FAILED", "Validation failed", ErrorType.VALIDATION),
    INTERNAL_ERROR("LOC_INTERNAL_ERROR", "Internal server error", ErrorType.SYSTEM),

    UNAUTHENTICATED("UNAUTHENTICATED", "Unauthenticated", ErrorType.AUTHENTICATION),
    FORBIDDEN("FORBIDDEN", "Forbidden", ErrorType.AUTHORIZATION),
    INVALID_API_KEY("INVALID_API_KEY", "Invalid API key", ErrorType.AUTHENTICATION),

    INVALID_KEY("INVALID_KEY", "Invalid key", ErrorType.VALIDATION),

    PROVINCE_ID_REQUIRED("PROVINCE_ID_REQUIRED", "Province ID is required", ErrorType.VALIDATION),
    PROVINCE_ID_TOO_LONG("PROVINCE_ID_TOO_LONG", "Province ID must not exceed 10 characters", ErrorType.VALIDATION),
    PROVINCE_CODE_REQUIRED("PROVINCE_CODE_REQUIRED", "Province code is required", ErrorType.VALIDATION),
    PROVINCE_CODE_TOO_LONG("PROVINCE_CODE_TOO_LONG", "Province code must not exceed 20 characters",
            ErrorType.VALIDATION),
    PROVINCE_NAME_REQUIRED("PROVINCE_NAME_REQUIRED", "Province name is required", ErrorType.VALIDATION),
    PROVINCE_NAME_TOO_LONG("PROVINCE_NAME_TOO_LONG", "Province name must not exceed 255 characters",
            ErrorType.VALIDATION),

    COMMUNE_ID_REQUIRED("COMMUNE_ID_REQUIRED", "Commune ID is required", ErrorType.VALIDATION),
    COMMUNE_CODE_REQUIRED("COMMUNE_CODE_REQUIRED", "Commune code is required", ErrorType.VALIDATION),
    COMMUNE_CODE_TOO_LONG("COMMUNE_CODE_TOO_LONG", "Commune code must not exceed 20 characters",
            ErrorType.VALIDATION),
    COMMUNE_NAME_REQUIRED("COMMUNE_NAME_REQUIRED", "Commune name is required", ErrorType.VALIDATION),
    COMMUNE_NAME_TOO_LONG("COMMUNE_NAME_TOO_LONG", "Commune name must not exceed 255 characters",
            ErrorType.VALIDATION),
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
