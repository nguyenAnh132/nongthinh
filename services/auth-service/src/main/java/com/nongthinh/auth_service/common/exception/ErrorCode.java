package com.nongthinh.auth_service.common.exception;

public enum ErrorCode {

    //AUTHENTICATION ERROR
    INVALID_TOKEN("AUTH_INVALID_TOKEN", "Invalid token", ErrorType.AUTHENTICATION),
    INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS", "Invalid credentials", ErrorType.AUTHENTICATION),
    INVALID_API_KEY("AUTH_INVALID_API_KEY", "Invalid API key", ErrorType.AUTHENTICATION),
    UNAUTHENTICATED("AUTH_UNAUTHENTICATED", "Unauthenticated", ErrorType.AUTHENTICATION),


    //AUTHORIZATION ERROR
    FORBIDDEN("AUTH_FORBIDDEN", "Forbidden", ErrorType.AUTHORIZATION),

    //INVALID KEY ERROR
    INVALID_KEY("AUTH_INVALID_KEY", "Invalid key", ErrorType.VALIDATION),

    //INTERNAL ERROR
    INTERNAL_ERROR("SYS_INTERNAL_ERROR", "Internal error", ErrorType.SYSTEM),

    //BUSINESS ERROR
    ROLES_REQUIRED("BUS_ROLES_REQUIRED", "Roles are required", ErrorType.BUSINESS_RULE),
    EMAIL_INVALID("BUS_EMAIL_INVALID", "Email is invalid", ErrorType.BUSINESS_RULE),
    EMAIL_FORMAT_INVALID("BUS_EMAIL_FORMAT_INVALID", "Email format is invalid", ErrorType.BUSINESS_RULE),
    FAILED_LOGIN_COUNT_INVALID("BUS_FAILED_LOGIN_COUNT_INVALID", "Failed login count is invalid", ErrorType.BUSINESS_RULE),
    ROLE_INVALID("BUS_ROLE_INVALID", "Role is invalid", ErrorType.BUSINESS_RULE),
    PERMISSION_INVALID("BUS_PERMISSION_INVALID", "Permission is invalid", ErrorType.BUSINESS_RULE),
    EMAIL_ALREADY_EXISTS("BUS_EMAIL_ALREADY_EXISTS", "Email already exists", ErrorType.BUSINESS_RULE),
    ROLE_CANNOT_BE_DELETED("BUS_ROLE_CANNOT_BE_DELETED", "Role cannot be deleted", ErrorType.BUSINESS_RULE),
    USER_DISABLED("BUS_USER_DISABLED", "User is disabled", ErrorType.BUSINESS_RULE),
    EMAIL_NOT_VERIFIED("BUS_EMAIL_NOT_VERIFIED", "Email is not verified", ErrorType.BUSINESS_RULE),
    SESSION_REVOKED("BUS_SESSION_REVOKED", "Session is revoked", ErrorType.BUSINESS_RULE),
    SESSION_REVOKED_REASON_REQUIRED("BUS_SESSION_REVOKED_REASON_REQUIRED", "Session revoked reason is required", ErrorType.BUSINESS_RULE),
    SESSION_INVALID("BUS_SESSION_INVALID", "Session is invalid", ErrorType.BUSINESS_RULE),
    SESSION_EXPIRES_AT_INVALID("BUS_SESSION_EXPIRES_AT_INVALID", "Session expires at is invalid", ErrorType.BUSINESS_RULE),
    USER_LOCKED("BUS_USER_LOCKED", "User is locked", ErrorType.BUSINESS_RULE),
    INVALID_AUTH_PROVIDER("BUS_INVALID_AUTH_PROVIDER", "Invalid auth provider for this operation", ErrorType.BUSINESS_RULE),

    
    //NOT FOUND ERROR
    ROLE_NOT_FOUND("NOT_FOUND_ROLE_NOT_FOUND", "Role not found", ErrorType.NOT_FOUND),
    USER_NOT_FOUND("NOT_FOUND_USER_NOT_FOUND", "User not found", ErrorType.NOT_FOUND),
    PERMISSION_NOT_FOUND("NOT_FOUND_PERMISSION_NOT_FOUND", "Permission not found", ErrorType.NOT_FOUND),
    SESSION_NOT_FOUND("NOT_FOUND_SESSION_NOT_FOUND", "Session not found", ErrorType.NOT_FOUND),
    PROFILE_NOT_FOUND("NOT_FOUND_PROFILE_NOT_FOUND", "Profile not found", ErrorType.NOT_FOUND),

    //SYSTEM ERROR
    MAX_FAILED_LOGIN_COUNT_INVALID("SYS_MAX_FAILED_LOGIN_COUNT_INVALID", "Max failed login count is invalid", ErrorType.SYSTEM),
    FAILED_LOGIN_LOCKOUT_DURATION_INVALID("SYS_FAILED_LOGIN_LOCKOUT_DURATION_INVALID", "Failed login lockout duration is invalid", ErrorType.SYSTEM),
    
    //INFRASTRUCTURE ERROR
    REFRESH_TOKEN_UNSUCCESSFUL("INF_REFRESH_TOKEN_UNSUCCESSFUL", "Refresh token unsuccessful", ErrorType.INFRASTRUCTURE),
    EVENT_SERIALIZATION_FAILED("INF_EVENT_SERIALIZATION_FAILED", "Failed to serialize event", ErrorType.INFRASTRUCTURE),
    KEYCLOAK_USER_ID_NOT_FOUND("INF_KEYCLOAK_USER_ID_NOT_FOUND", "Keycloak user ID not found", ErrorType.INFRASTRUCTURE),
    KEYCLOAK_USER_CREATION_FAILED("INF_KEYCLOAK_USER_CREATION_FAILED", "Failed to create Keycloak user account", ErrorType.INFRASTRUCTURE),
    KEYCLOAK_EXCHANGE_CLIENT_TOKEN_FAILED("INF_KEYCLOAK_EXCHANGE_CLIENT_TOKEN_FAILED", "Failed to exchange keycloak client token", ErrorType.INFRASTRUCTURE),
    KEYCLOAK_LOGOUT_FAILED("INF_KEYCLOAK_LOGOUT_FAILED", "Failed to logout user account", ErrorType.INFRASTRUCTURE),
    KEYCLOAK_GET_ROLE_BY_NAME_FAILED("INF_KEYCLOAK_GET_ROLE_BY_NAME_FAILED", "Failed to get role by name", ErrorType.INFRASTRUCTURE),
    KEYCLOAK_ROLE_MAPPING_FAILED("INF_KEYCLOAK_ROLE_MAPPING_FAILED", "Failed to map role", ErrorType.INFRASTRUCTURE),
    PROFILE_SERVICE_GET_FARMER_PROFILE_FAILED("INF_PROFILE_SERVICE_GET_FARMER_PROFILE_FAILED", "Failed to get farmer profile", ErrorType.INFRASTRUCTURE),
    PROFILE_SERVICE_GET_BRAND_PROFILE_FAILED("INF_PROFILE_SERVICE_GET_BRAND_PROFILE_FAILED", "Failed to get brand profile", ErrorType.INFRASTRUCTURE),
    PROFILE_SERVICE_GET_ADMIN_PROFILE_FAILED("INF_PROFILE_SERVICE_GET_ADMIN_PROFILE_FAILED", "Failed to get admin profile", ErrorType.INFRASTRUCTURE),
    KAFKA_PUBLISH_FAILED("INF_KAFKA_PUBLISH_FAILED", "Failed to publish event", ErrorType.INFRASTRUCTURE),
    OTP_PERSISTENCE_FAILED("INF_OTP_PERSISTENCE_FAILED", "OTP storage is unavailable", ErrorType.INFRASTRUCTURE),
    
    //VALIDATION ERROR
    PASSWORD_REQUIRED("VAL_PASSWORD_REQUIRED", "Password is required", ErrorType.VALIDATION),
    EMAIL_REQUEST_FORMAT_INVALID("VAL_EMAIL_FORMAT_INVALID", "Email format is invalid", ErrorType.VALIDATION),
    TEMPORARY_REQUIRED("VAL_TEMPORARY_REQUIRED", "Temporary is required", ErrorType.VALIDATION),
    ENABLED_REQUIRED("VAL_ENABLED_REQUIRED", "Enabled is required", ErrorType.VALIDATION),
    FIRST_NAME_REQUIRED("VAL_FIRST_NAME_REQUIRED", "First name is required", ErrorType.VALIDATION),
    LAST_NAME_REQUIRED("VAL_LAST_NAME_REQUIRED", "Last name is required", ErrorType.VALIDATION),
    FIRST_NAME_LENGTH_INVALID("VAL_FIRST_NAME_LENGTH_INVALID", "First name must be between 1 and 255 characters", ErrorType.VALIDATION),
    LAST_NAME_LENGTH_INVALID("VAL_LAST_NAME_LENGTH_INVALID", "Last name must be between 3 and 255 characters", ErrorType.VALIDATION),
    GENDER_REQUIRED("VAL_GENDER_REQUIRED", "Gender is required", ErrorType.VALIDATION),
    GENDER_INVALID("VAL_GENDER_INVALID", "Gender is invalid", ErrorType.VALIDATION),
    PHONE_REQUIRED("VAL_PHONE_REQUIRED", "Phone is required", ErrorType.VALIDATION),
    PHONE_INVALID("VAL_PHONE_INVALID", "Phone is invalid", ErrorType.VALIDATION),
    PROVINCE_ID_INVALID("VAL_PROVINCE_ID_INVALID", "Province ID is invalid", ErrorType.VALIDATION),
    ADDRESS_DETAIL_TOO_LONG("VAL_ADDRESS_DETAIL_TOO_LONG", "Address detail must not exceed 500 characters", ErrorType.VALIDATION),
    AVATAR_URL_INVALID("VAL_AVATAR_URL_INVALID", "Avatar URL is invalid", ErrorType.VALIDATION),
    OTP_LENGTH_INVALID("VAL_OTP_LENGTH_INVALID", "OTP length is invalid", ErrorType.VALIDATION),
    OTP_REQUIRED("VAL_OTP_REQUIRED", "OTP is required", ErrorType.VALIDATION),

    OTP_INVALID("BUS_OTP_INVALID", "OTP is invalid", ErrorType.BUSINESS_RULE),
    OTP_EXPIRED("BUS_OTP_EXPIRED", "OTP has expired", ErrorType.BUSINESS_RULE),
    OTP_ATTEMPT_LIMIT_EXCEEDED("BUS_OTP_ATTEMPT_LIMIT_EXCEEDED", "OTP attempt limit exceeded", ErrorType.BUSINESS_RULE),
    OTP_RESEND_TOO_FREQUENT("BUS_OTP_RESEND_TOO_FREQUENT", "OTP resend is too frequent", ErrorType.BUSINESS_RULE),
    OTP_RESEND_LIMIT_EXCEEDED("BUS_OTP_RESEND_LIMIT_EXCEEDED", "OTP resend limit exceeded", ErrorType.BUSINESS_RULE),
    EMAIL_ALREADY_VERIFIED("BUS_EMAIL_ALREADY_VERIFIED", "Email is already verified", ErrorType.BUSINESS_RULE),
    ADMIN_GROUP_REQUIRED("VAL_ADMIN_GROUP_REQUIRED", "Admin group is required", ErrorType.VALIDATION),
    ADMIN_GROUP_INVALID("VAL_ADMIN_GROUP_INVALID", "Admin group is invalid", ErrorType.VALIDATION),
    BRAND_NAME_REQUIRED("VAL_BRAND_NAME_REQUIRED", "Brand name is required", ErrorType.VALIDATION),
    BRAND_NAME_LENGTH_INVALID("VAL_BRAND_NAME_LENGTH_INVALID", "Brand name must be between {min} and {max} characters", ErrorType.VALIDATION),
    TAX_CODE_TOO_LONG("VAL_TAX_CODE_TOO_LONG", "Tax code must not exceed 20 characters", ErrorType.VALIDATION),
    DESCRIPTION_TOO_LONG("VAL_DESCRIPTION_TOO_LONG", "Description must not exceed 1000 characters", ErrorType.VALIDATION),
    REPRESENTATIVE_NAME_REQUIRED("VAL_REPRESENTATIVE_NAME_REQUIRED", "Representative name is required", ErrorType.VALIDATION),
    REPRESENTATIVE_PHONE_REQUIRED("VAL_REPRESENTATIVE_PHONE_REQUIRED", "Representative phone is required", ErrorType.VALIDATION),
    REPRESENTATIVE_EMAIL_REQUIRED("VAL_REPRESENTATIVE_EMAIL_REQUIRED", "Representative email is required", ErrorType.VALIDATION),
    REPRESENTATIVE_EMAIL_INVALID("VAL_REPRESENTATIVE_EMAIL_INVALID", "Representative email is invalid", ErrorType.VALIDATION),
    LOGO_URL_INVALID("VAL_LOGO_URL_INVALID", "Logo URL is invalid", ErrorType.VALIDATION),
    BANNER_URL_INVALID("VAL_BANNER_URL_INVALID", "Banner URL is invalid", ErrorType.VALIDATION),
    WEBSITE_URL_INVALID("VAL_WEBSITE_URL_INVALID", "Website URL is invalid", ErrorType.VALIDATION),
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
