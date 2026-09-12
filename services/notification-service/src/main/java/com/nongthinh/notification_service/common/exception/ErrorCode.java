package com.nongthinh.notification_service.common.exception;

public enum ErrorCode {
    NOTIFICATION_NOT_FOUND("NF_NOTIFICATION_NOT_FOUND", "Notification not found", ErrorType.NOT_FOUND),
    NOTIFICATION_REQUEST_INVALID("VAL_NOTIFICATION_REQUEST_INVALID", "Invalid notification request", ErrorType.VALIDATION),

    //BUSINESS_RULE

    EMAIL_SEND_FAILED("EMAIL_SEND_FAILED", "Failed to send email", ErrorType.SYSTEM),
    EVENT_DESERIALIZATION_FAILED("EVENT_DESERIALIZATION_FAILED", "Failed to deserialize event", ErrorType.SYSTEM),

    UNAUTHENTICATED("UNAUTHENTICATED", "Unauthenticated", ErrorType.AUTHENTICATION),
    INVALID_API_KEY("INVALID_API_KEY", "Invalid API key", ErrorType.AUTHENTICATION),
    FORBIDDEN("FORBIDDEN", "Forbidden", ErrorType.AUTHORIZATION),
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal error", ErrorType.SYSTEM),
    INVALID_KEY("INVALID_KEY", "Invalid key", ErrorType.VALIDATION),

    //NOT_FOUND
    EMAIL_HISTORY_NOT_FOUND("EMAIL_HISTORY_NOT_FOUND", "Email history not found", ErrorType.NOT_FOUND),
    EMAIL_TEMPLATE_PURPOSE_NOT_FOUND("EMAIL_TEMPLATE_PURPOSE_NOT_FOUND", "Email template purpose not found", ErrorType.NOT_FOUND),
    EMAIL_TEMPLATE_NOT_FOUND("EMAIL_TEMPLATE_NOT_FOUND", "Email template not found", ErrorType.NOT_FOUND),
    ACTIVE_EMAIL_TEMPLATE_NOT_FOUND("ACTIVE_EMAIL_TEMPLATE_NOT_FOUND", "Active email template not found", ErrorType.NOT_FOUND),
    EMAIL_TEMPLATE_INVALID_PLACEHOLDER("EMAIL_TEMPLATE_INVALID_PLACEHOLDER", "Email template contains invalid placeholders", ErrorType.BUSINESS_RULE),
    EMAIL_TEMPLATE_MISSING_REQUIRED_VARIABLE("EMAIL_TEMPLATE_MISSING_REQUIRED_VARIABLE", "Missing required email template variables", ErrorType.BUSINESS_RULE),
    EMAIL_TEMPLATE_PURPOSE_CANNOT_DELETE("EMAIL_TEMPLATE_PURPOSE_CANNOT_DELETE", "System-defined email template purpose cannot be deleted", ErrorType.BUSINESS_RULE),

    TEMPLATE_NAME_REQUIRED("TEMPLATE_NAME_REQUIRED", "Template name is required", ErrorType.VALIDATION),
    TEMPLATE_SUBJECT_REQUIRED("TEMPLATE_SUBJECT_REQUIRED", "Template subject is required", ErrorType.VALIDATION),
    TEMPLATE_CONTENT_REQUIRED("TEMPLATE_CONTENT_REQUIRED", "Template html or text content is required", ErrorType.VALIDATION),
    PURPOSE_NAME_REQUIRED("PURPOSE_NAME_REQUIRED", "Purpose name is required", ErrorType.VALIDATION),
    PURPOSE_CODE_REQUIRED("PURPOSE_CODE_REQUIRED", "Purpose code is required", ErrorType.VALIDATION),
    EMAIL_TO_REQUIRED("EMAIL_TO_REQUIRED", "Recipient email is required", ErrorType.VALIDATION),
    OTP_REQUIRED("OTP_REQUIRED", "OTP is required", ErrorType.VALIDATION),
    PURPOSE_CODE_INVALID("PURPOSE_CODE_INVALID", "Purpose code is invalid", ErrorType.VALIDATION),
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
