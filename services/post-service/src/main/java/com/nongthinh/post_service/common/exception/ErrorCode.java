package com.nongthinh.post_service.common.exception;

public enum ErrorCode {
    UNAUTHENTICATED("AUTH_UNAUTHENTICATED", "Unauthenticated", ErrorType.AUTHENTICATION),
    FORBIDDEN("AUTH_FORBIDDEN", "Forbidden", ErrorType.AUTHORIZATION),
    INTERNAL_ERROR("SYS_INTERNAL_ERROR", "Internal error", ErrorType.SYSTEM),
    INVALID_REQUEST_PARAMETER("VAL_INVALID_REQUEST_PARAMETER", "Invalid request parameter", ErrorType.VALIDATION),

    POST_TYPE_NOT_FOUND("NOT_FOUND_POST_TYPE_NOT_FOUND", "Post type not found", ErrorType.NOT_FOUND),
    POST_TOPIC_NOT_FOUND("NOT_FOUND_POST_TOPIC_NOT_FOUND", "Post topic not found", ErrorType.NOT_FOUND),
    POST_TYPE_CODE_ALREADY_EXISTS("BUS_POST_TYPE_CODE_ALREADY_EXISTS", "Post type code already exists", ErrorType.BUSINESS_RULE),
    POST_TOPIC_SLUG_ALREADY_EXISTS("BUS_POST_TOPIC_SLUG_ALREADY_EXISTS", "Post topic slug already exists", ErrorType.BUSINESS_RULE),
    POST_TYPE_IN_USE("BUS_POST_TYPE_IN_USE", "Post type is in use", ErrorType.BUSINESS_RULE),
    POST_TOPIC_IN_USE("BUS_POST_TOPIC_IN_USE", "Post topic is in use", ErrorType.BUSINESS_RULE),

    POST_NOT_FOUND("NOT_FOUND_POST_NOT_FOUND", "Post not found", ErrorType.NOT_FOUND),
    POST_MEDIA_NOT_FOUND("NOT_FOUND_POST_MEDIA_NOT_FOUND", "Post media not found", ErrorType.NOT_FOUND),
    POST_CROP_TYPE_NOT_FOUND("NOT_FOUND_POST_CROP_TYPE_NOT_FOUND", "Crop type not found or inactive", ErrorType.NOT_FOUND),
    COMMENT_NOT_FOUND("NOT_FOUND_COMMENT_NOT_FOUND", "Comment not found", ErrorType.NOT_FOUND),
    POST_REPORT_NOT_FOUND("NOT_FOUND_POST_REPORT_NOT_FOUND", "Post report not found", ErrorType.NOT_FOUND),
    POST_HISTORY_NOT_FOUND("NOT_FOUND_POST_HISTORY_NOT_FOUND", "Post history not found", ErrorType.NOT_FOUND),
    FILE_NOT_FOUND("NOT_FOUND_FILE_NOT_FOUND", "File not found", ErrorType.NOT_FOUND),
    POST_ACCESS_DENIED("AUTH_POST_ACCESS_DENIED", "Post access denied", ErrorType.AUTHORIZATION),
    POST_NOT_EDITABLE("BUS_POST_NOT_EDITABLE", "Post cannot be edited in its current state", ErrorType.BUSINESS_RULE),
    POST_NOT_PUBLISHABLE("BUS_POST_NOT_PUBLISHABLE", "Only a draft post can be published", ErrorType.BUSINESS_RULE),
    POST_VISIBILITY_NOT_ALLOWED("BUS_POST_VISIBILITY_NOT_ALLOWED", "Post visibility is not enabled", ErrorType.BUSINESS_RULE),
    POST_MEDIA_FILE_INVALID("BUS_POST_MEDIA_FILE_INVALID", "File is not valid post media", ErrorType.BUSINESS_RULE),
    POST_MEDIA_CONFLICT("BUS_POST_MEDIA_CONFLICT", "Media file or display order is already used in the post", ErrorType.BUSINESS_RULE),
    POST_MEDIA_LIMIT_EXCEEDED("BUS_POST_MEDIA_LIMIT_EXCEEDED", "Post media limit exceeded", ErrorType.BUSINESS_RULE),
    COMMENT_ACCESS_DENIED("AUTH_COMMENT_ACCESS_DENIED", "Comment access denied", ErrorType.AUTHORIZATION),
    COMMENT_NOT_EDITABLE("BUS_COMMENT_NOT_EDITABLE", "Comment cannot be edited in its current state", ErrorType.BUSINESS_RULE),
    COMMENT_REPLY_DEPTH_EXCEEDED("BUS_COMMENT_REPLY_DEPTH_EXCEEDED", "Replies are limited to one level", ErrorType.BUSINESS_RULE),
    POST_ALREADY_REPORTED("BUS_POST_ALREADY_REPORTED", "Post has already been reported by this user", ErrorType.BUSINESS_RULE),
    POST_REPORT_SELF_NOT_ALLOWED("BUS_POST_REPORT_SELF_NOT_ALLOWED", "A post author cannot report their own post", ErrorType.BUSINESS_RULE),
    POST_REPORT_STATUS_CONFLICT("BUS_POST_REPORT_STATUS_CONFLICT", "Post report cannot transition from its current status", ErrorType.BUSINESS_RULE),
    FILE_SERVICE_UNAVAILABLE("INF_FILE_SERVICE_UNAVAILABLE", "File service is unavailable", ErrorType.INFRASTRUCTURE),
    AGRI_CATALOG_SERVICE_UNAVAILABLE("INF_AGRI_CATALOG_SERVICE_UNAVAILABLE", "Agri catalog service is unavailable", ErrorType.INFRASTRUCTURE),

    POST_TYPE_ID_REQUIRED("VAL_POST_TYPE_ID_REQUIRED", "Post type id is required", ErrorType.VALIDATION),
    POST_CONTENT_REQUIRED("VAL_POST_CONTENT_REQUIRED", "Post content is required", ErrorType.VALIDATION),
    POST_CONTENT_TOO_LONG("VAL_POST_CONTENT_TOO_LONG", "Post content is too long", ErrorType.VALIDATION),
    POST_LOCATION_TOO_LONG("VAL_POST_LOCATION_TOO_LONG", "Post location must not exceed 120 characters", ErrorType.VALIDATION),
    POST_VISIBILITY_REQUIRED("VAL_POST_VISIBILITY_REQUIRED", "Post visibility is required", ErrorType.VALIDATION),
    POST_STATUS_REQUIRED("VAL_POST_STATUS_REQUIRED", "Post status is required", ErrorType.VALIDATION),
    POST_STATUS_INVALID("VAL_POST_STATUS_INVALID", "Post status must be DRAFT or PUBLISHED", ErrorType.VALIDATION),
    POST_CROP_TYPE_IDS_REQUIRED("VAL_POST_CROP_TYPE_IDS_REQUIRED", "Crop type ids are required", ErrorType.VALIDATION),
    POST_MEDIA_FILE_ID_REQUIRED("VAL_POST_MEDIA_FILE_ID_REQUIRED", "Post media file id is required", ErrorType.VALIDATION),
    POST_MEDIA_DISPLAY_ORDER_INVALID("VAL_POST_MEDIA_DISPLAY_ORDER_INVALID", "Post media display order must not be negative", ErrorType.VALIDATION),
    POST_MEDIA_CAPTION_TOO_LONG("VAL_POST_MEDIA_CAPTION_TOO_LONG", "Post media caption must not exceed 500 characters", ErrorType.VALIDATION),
    COMMENT_CONTENT_REQUIRED("VAL_COMMENT_CONTENT_REQUIRED", "Comment content is required", ErrorType.VALIDATION),
    COMMENT_CONTENT_TOO_LONG("VAL_COMMENT_CONTENT_TOO_LONG", "Comment content must not exceed 2000 characters", ErrorType.VALIDATION),
    REACTION_TYPE_REQUIRED("VAL_REACTION_TYPE_REQUIRED", "Reaction type is required", ErrorType.VALIDATION),
    POST_REPORT_REASON_REQUIRED("VAL_POST_REPORT_REASON_REQUIRED", "Post report reason is required", ErrorType.VALIDATION),
    POST_REPORT_REASON_DETAIL_INVALID("VAL_POST_REPORT_REASON_DETAIL_INVALID", "Post report reason detail must not be blank", ErrorType.VALIDATION),
    POST_REPORT_REASON_DETAIL_TOO_LONG("VAL_POST_REPORT_REASON_DETAIL_TOO_LONG", "Post report reason detail must not exceed 10000 characters", ErrorType.VALIDATION),
    POST_REPORT_RESOLUTION_NOTE_INVALID("VAL_POST_REPORT_RESOLUTION_NOTE_INVALID", "Post report resolution note must not be blank", ErrorType.VALIDATION),
    POST_REPORT_RESOLUTION_NOTE_TOO_LONG("VAL_POST_REPORT_RESOLUTION_NOTE_TOO_LONG", "Post report resolution note must not exceed 10000 characters", ErrorType.VALIDATION),
    POST_REPORT_MODERATION_ACTION_REQUIRED("VAL_POST_REPORT_MODERATION_ACTION_REQUIRED", "A moderation action is required when resolving a report", ErrorType.VALIDATION),
    POST_REPORT_POST_MODERATION_CONFLICT("BUS_POST_REPORT_POST_MODERATION_CONFLICT", "The reported post cannot be moderated in its current state", ErrorType.BUSINESS_RULE),

    POST_TYPE_CODE_REQUIRED("VAL_POST_TYPE_CODE_REQUIRED", "Post type code is required", ErrorType.VALIDATION),
    POST_TYPE_CODE_TOO_LONG("VAL_POST_TYPE_CODE_TOO_LONG", "Post type code must not exceed 50 characters", ErrorType.VALIDATION),
    POST_TYPE_CODE_INVALID("VAL_POST_TYPE_CODE_INVALID", "Post type code must be uppercase snake case", ErrorType.VALIDATION),
    POST_TOPIC_SLUG_REQUIRED("VAL_POST_TOPIC_SLUG_REQUIRED", "Post topic slug is required", ErrorType.VALIDATION),
    POST_TOPIC_SLUG_TOO_LONG("VAL_POST_TOPIC_SLUG_TOO_LONG", "Post topic slug must not exceed 180 characters", ErrorType.VALIDATION),
    POST_TOPIC_SLUG_INVALID("VAL_POST_TOPIC_SLUG_INVALID", "Post topic slug must be lowercase kebab case", ErrorType.VALIDATION),
    POST_CATALOG_NAME_REQUIRED("VAL_POST_CATALOG_NAME_REQUIRED", "Name is required", ErrorType.VALIDATION),
    POST_CATALOG_NAME_TOO_LONG("VAL_POST_CATALOG_NAME_TOO_LONG", "Name must not exceed 150 characters", ErrorType.VALIDATION),
    POST_CATALOG_DESCRIPTION_TOO_LONG("VAL_POST_CATALOG_DESCRIPTION_TOO_LONG", "Description must not exceed 500 characters", ErrorType.VALIDATION),
    POST_CATALOG_DISPLAY_ORDER_INVALID("VAL_POST_CATALOG_DISPLAY_ORDER_INVALID", "Display order must not be negative", ErrorType.VALIDATION),
    POST_CATALOG_ACTIVE_REQUIRED("VAL_POST_CATALOG_ACTIVE_REQUIRED", "Active status is required", ErrorType.VALIDATION);

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
