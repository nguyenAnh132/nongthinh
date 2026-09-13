package com.nongthinh.profile_service.common.exception;

public enum ErrorCode {
    SELF_FOLLOW_NOT_ALLOWED("PRO_SELF_FOLLOW_NOT_ALLOWED", "Cannot follow yourself", ErrorType.BUSINESS_RULE),

    PROFILE_UNDER_REVIEW("PRO_PROFILE_UNDER_REVIEW", "Profile is under review", ErrorType.BUSINESS_RULE),
    PROFILE_NEEDS_REVISION("PRO_PROFILE_NEEDS_REVISION", "Profile needs revision", ErrorType.BUSINESS_RULE),
    PROFILE_READY_FOR_FINAL_REVIEW("PRO_PROFILE_READY_FOR_FINAL_REVIEW", "Profile is ready for final review", ErrorType.BUSINESS_RULE),
    GENDER_IS_INVALID("PRO_GENDER_INVALID", "Gender is invalid", ErrorType.BUSINESS_RULE),
    PERSON_NAME_IS_INVALID("PRO_PERSON_NAME_INVALID", "Person name is invalid", ErrorType.BUSINESS_RULE),
    BRAND_NAME_INVALID("PRO_BRAND_NAME_INVALID", "Brand name is invalid", ErrorType.BUSINESS_RULE),
    ADDRESS_DETAIL_INVALID("PRO_ADDRESS_DETAIL_INVALID", "Address detail is invalid", ErrorType.BUSINESS_RULE),
    PROVINCE_ID_INVALID("PRO_PROVINCE_ID_INVALID", "Province ID is invalid", ErrorType.BUSINESS_RULE),
    ADDRESS_LOCATION_INVALID("PRO_ADDRESS_LOCATION_INVALID",
            "Address is invalid: province or commune does not exist or does not match",
            ErrorType.BUSINESS_RULE),
    PROFILE_ALREADY_EXISTS("PRO_PROFILE_ALREADY_EXISTS", "Profile already exists for this user",
            ErrorType.BUSINESS_RULE),
    PROFILE_STATUS_INVALID("PRO_PROFILE_STATUS_INVALID", "Profile status is invalid", ErrorType.BUSINESS_RULE),
    REJECTION_REASON_REQUIRED("PRO_REJECTION_REASON_REQUIRED", "Rejection reason is required", ErrorType.VALIDATION),
    PROFILE_LOCKED("PRO_PROFILE_LOCKED", "Profile is locked", ErrorType.BUSINESS_RULE),
    PROFILE_DISABLED("PRO_PROFILE_DISABLED", "Profile is disabled", ErrorType.BUSINESS_RULE),
    PROFILE_DELETED("PRO_PROFILE_DELETED", "Profile is deleted", ErrorType.BUSINESS_RULE),
    PROFILE_PENDING_APPROVAL("PRO_PROFILE_PENDING_APPROVAL", "Profile is pending approval", ErrorType.BUSINESS_RULE),
    PROFILE_REJECTED("PRO_PROFILE_REJECTED", "Profile is rejected", ErrorType.BUSINESS_RULE),
    PROFILE_NOT_FOUND("PRO_PROFILE_NOT_FOUND", "Profile not found", ErrorType.NOT_FOUND),
    VERIFICATION_RESULT_INVALID("PRO_VERIFICATION_RESULT_INVALID", "Verification result is invalid", ErrorType.BUSINESS_RULE),
    BRAND_DOCUMENT_REVIEW_STATUS_INVALID("PRO_BRAND_DOCUMENT_REVIEW_STATUS_INVALID",
            "Brand document review status is invalid", ErrorType.BUSINESS_RULE),
    BRAND_DOCUMENT_NOT_FOUND("PRO_BRAND_DOCUMENT_NOT_FOUND", "Brand document not found", ErrorType.NOT_FOUND),
    REVISION_REASON_REQUIRED("PRO_REVISION_REASON_REQUIRED", "Revision reason is required", ErrorType.VALIDATION),
    BRAND_LIFECYCLE_ACTION_INVALID("PRO_BRAND_LIFECYCLE_ACTION_INVALID", "Brand lifecycle action is invalid",
            ErrorType.BUSINESS_RULE),

    LOCATION_SERVICE_UNAVAILABLE("PRO_LOCATION_SERVICE_UNAVAILABLE",
            "Location service is unavailable", ErrorType.INTERNAL_SERVICE),
    EVENT_DESERIALIZATION_FAILED("PRO_EVENT_DESERIALIZATION_FAILED",
            "Failed to deserialize event", ErrorType.SYSTEM),

    VALIDATION_FAILED("PRO_VALIDATION_FAILED", "Validation failed", ErrorType.VALIDATION),
    INTERNAL_ERROR("PRO_INTERNAL_ERROR", "Internal server error", ErrorType.SYSTEM),

    UNAUTHENTICATED("UNAUTHENTICATED", "Unauthenticated", ErrorType.AUTHENTICATION),

    // AUTHORIZATION ERROR
    FORBIDDEN("FORBIDDEN", "Forbidden", ErrorType.AUTHORIZATION),

    // INVALID KEY ERROR
    INVALID_KEY("INVALID_KEY", "Invalid key", ErrorType.VALIDATION),

    // VALIDATION ERROR
    USER_ID_REQUIRED("USER_ID_REQUIRED", "User ID is required", ErrorType.VALIDATION),
    PERSON_NAME_INVALID("PERSON_NAME_INVALID", "{type} must be between {min} and {max} characters", ErrorType.VALIDATION),
    GENDER_REQUIRED("GENDER_REQUIRED", "Gender is required", ErrorType.VALIDATION),
    GENDER_INVALID("GENDER_INVALID", "Gender is invalid", ErrorType.VALIDATION),
    FIRST_NAME_REQUIRED("FIRST_NAME_REQUIRED", "First name is required", ErrorType.VALIDATION),
    LAST_NAME_REQUIRED("LAST_NAME_REQUIRED", "Last name is required", ErrorType.VALIDATION),
    PHONE_INVALID("PHONE_INVALID", "Phone is invalid", ErrorType.VALIDATION),
    PHONE_REQUIRED("PHONE_REQUIRED", "Phone is required", ErrorType.VALIDATION),

    BRAND_NAME_REQUIRED("BRAND_NAME_REQUIRED", "Brand name is required", ErrorType.VALIDATION),
    BRAND_NAME_LENGTH_INVALID("BRAND_NAME_LENGTH_INVALID", "{type} must be between {min} and {max} characters",
            ErrorType.VALIDATION),
    BRAND_NAME_FORMAT_INVALID("BRAND_NAME_FORMAT_INVALID", "Brand name format is invalid", ErrorType.VALIDATION),
    TAX_CODE_TOO_LONG("TAX_CODE_TOO_LONG", "Tax code must not exceed 20 characters", ErrorType.VALIDATION),

    REPRESENTATIVE_NAME_REQUIRED("REPRESENTATIVE_NAME_REQUIRED", "Representative name is required",
            ErrorType.VALIDATION),
    REPRESENTATIVE_PHONE_REQUIRED("REPRESENTATIVE_PHONE_REQUIRED", "Representative phone is required",
            ErrorType.VALIDATION),
    REPRESENTATIVE_EMAIL_REQUIRED("REPRESENTATIVE_EMAIL_REQUIRED", "Representative email is required",
            ErrorType.VALIDATION),
    REPRESENTATIVE_EMAIL_INVALID("REPRESENTATIVE_EMAIL_INVALID", "Representative email is invalid",
            ErrorType.VALIDATION),

    DESCRIPTION_TOO_LONG("DESCRIPTION_TOO_LONG", "Description must not exceed 1000 characters", ErrorType.VALIDATION),
    ADDRESS_DETAIL_TOO_LONG("ADDRESS_DETAIL_TOO_LONG", "Address detail must not exceed 500 characters",
            ErrorType.VALIDATION),

    AVATAR_URL_INVALID("AVATAR_URL_INVALID", "Avatar URL is invalid", ErrorType.VALIDATION),
    LOGO_URL_INVALID("LOGO_URL_INVALID", "Logo URL is invalid", ErrorType.VALIDATION),
    BANNER_URL_INVALID("BANNER_URL_INVALID", "Banner URL is invalid", ErrorType.VALIDATION),
    WEBSITE_URL_INVALID("WEBSITE_URL_INVALID", "Website URL is invalid", ErrorType.VALIDATION),

    URL_REQUIRED("URL_REQUIRED", "URL is required", ErrorType.VALIDATION),
    URL_INVALID("URL_INVALID", "URL is invalid", ErrorType.VALIDATION),

    DEPARTMENT_ID_REQUIRED("DEPARTMENT_ID_REQUIRED", "Department ID is required", ErrorType.VALIDATION),
    POSITION_ID_REQUIRED("POSITION_ID_REQUIRED", "Position ID is required", ErrorType.VALIDATION),

    PROVINCE_ID_REQUIRED("PROVINCE_ID_REQUIRED",
            "Province ID is required when commune ID is provided", ErrorType.VALIDATION),
    COMMUNE_ID_REQUIRED("COMMUNE_ID_REQUIRED",
            "Commune ID is required when province ID is provided", ErrorType.VALIDATION),
    
    
    INVALID_API_KEY("INVALID_API_KEY", "Invalid API key", ErrorType.AUTHENTICATION),
    
    // INFRASTRUCTURE ERROR
    EVENT_SERIALIZATION_FAILED("PRO_EVENT_SERIALIZATION_FAILED", "Failed to serialize event", ErrorType.SYSTEM),
    KAFKA_PUBLISH_FAILED("PRO_KAFKA_PUBLISH_FAILED", "Failed to publish event to Kafka", ErrorType.SYSTEM),
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
