package com.nongthinh.brand_service.common.exception;

public enum ErrorCode {

    INVALID_API_KEY("INTERNAL_API_KEY_INVALID", "Invalid API key", ErrorType.AUTHENTICATION),

    UNAUTHENTICATED("INTERNAL_UNAUTHENTICATED", "Unauthenticated", ErrorType.AUTHENTICATION),

    INVALID_TOKEN("INTERNAL_INVALID_TOKEN", "Invalid token", ErrorType.AUTHENTICATION),
    ADMIN_GROUP_INVALID("INTERNAL_ADMIN_GROUP_INVALID", "Invalid admin group", ErrorType.AUTHENTICATION),

    ROLES_REQUIRED("INTERNAL_ROLES_REQUIRED", "Roles are required", ErrorType.AUTHENTICATION),
    EVENT_DESERIALIZATION_FAILED("INTERNAL_EVENT_DESERIALIZATION_FAILED", "Failed to deserialize event", ErrorType.INTERNAL_SERVICE),
    EVENT_SERIALIZATION_FAILED("INTERNAL_EVENT_SERIALIZATION_FAILED", "Failed to serialize event", ErrorType.INTERNAL_SERVICE),
    KAFKA_PUBLISH_FAILED("INTERNAL_KAFKA_PUBLISH_FAILED", "Failed to publish event to Kafka", ErrorType.INTERNAL_SERVICE),

    TASK_NOT_FOUND("BRAND_TASK_NOT_FOUND", "Task not found", ErrorType.NOT_FOUND),
    TASK_NOT_ASSIGNED("BRAND_TASK_NOT_ASSIGNED", "Task is not assigned to you", ErrorType.AUTHORIZATION),
    TASK_ALREADY_CLAIMED("BRAND_TASK_ALREADY_CLAIMED", "Task already claimed", ErrorType.BUSINESS_RULE),
    TASK_NOT_CLAIMED("BRAND_TASK_NOT_CLAIMED", "Task is not claimed", ErrorType.BUSINESS_RULE),
    TASK_DEFINITION_MISMATCH("BRAND_TASK_DEFINITION_MISMATCH", "Invalid task type", ErrorType.VALIDATION),
    PROFILE_NOT_FOUND("BRAND_PROFILE_NOT_FOUND", "Brand profile not found", ErrorType.NOT_FOUND),
    PROCESS_VARIABLE_MISSING("BRAND_PROCESS_VARIABLE_MISSING", "Required process variable is missing", ErrorType.BUSINESS_RULE),
    REVISION_REASON_REQUIRED("BRAND_REVISION_REASON_REQUIRED", "Revision reason is required", ErrorType.VALIDATION),
    REJECTION_REASON_REQUIRED("BRAND_REJECTION_REASON_REQUIRED", "Rejection reason is required", ErrorType.VALIDATION),
    OUTCOME_INVALID("BRAND_OUTCOME_INVALID", "Outcome is invalid", ErrorType.VALIDATION),
    APPROVAL_PROCESS_NOT_FOUND("BRAND_APPROVAL_PROCESS_NOT_FOUND", "Brand approval process not found", ErrorType.NOT_FOUND),
    PROCESS_MESSAGE_CORRELATION_FAILED("BRAND_PROCESS_MESSAGE_CORRELATION_FAILED", "Failed to correlate process message", ErrorType.BUSINESS_RULE),
    FORBIDDEN("BRAND_FORBIDDEN", "Forbidden", ErrorType.AUTHORIZATION),
    VALIDATION_FAILED("BRAND_VALIDATION_FAILED", "Validation failed", ErrorType.VALIDATION),
    REVISION_REASON_TOO_LONG("BRAND_REVISION_REASON_TOO_LONG", "Revision reason must not exceed {max} characters", ErrorType.VALIDATION),
    REJECTION_REASON_TOO_LONG("BRAND_REJECTION_REASON_TOO_LONG", "Rejection reason must not exceed {max} characters", ErrorType.VALIDATION),
    PHONE_REQUIRED("BRAND_PHONE_REQUIRED", "Phone number is required", ErrorType.VALIDATION),
    PHONE_INVALID("BRAND_PHONE_INVALID", "Phone number must contain exactly 10 digits", ErrorType.VALIDATION),
    VERIFICATION_RESULT_INVALID("BRAND_VERIFICATION_RESULT_INVALID", "Verification result is invalid", ErrorType.VALIDATION),
    NOTE_TOO_LONG("BRAND_NOTE_TOO_LONG", "Note must not exceed {max} characters", ErrorType.VALIDATION),
    INTERNAL_ERROR("BRAND_INTERNAL_ERROR", "Internal server error", ErrorType.SYSTEM),
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
