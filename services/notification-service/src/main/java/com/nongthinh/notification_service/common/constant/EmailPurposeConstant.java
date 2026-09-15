package com.nongthinh.notification_service.common.constant;

import org.springframework.stereotype.Component;

@Component
public final class EmailPurposeConstant {

    public static final String RESET_PASSWORD = "RESET_PASSWORD";
    public static final String BRAND_APPROVED = "BRAND_APPROVED";
    public static final String BRAND_REJECTED = "BRAND_REJECTED";
    public static final String BRAND_NEEDS_REVISION = "BRAND_NEEDS_REVISION";
    public static final String BRAND_DOCUMENTS_REQUESTED = "BRAND_DOCUMENTS_REQUESTED";
    public static final String WELCOME = "WELCOME";

    private EmailPurposeConstant() {}

}
