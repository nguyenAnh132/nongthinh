package com.nongthinh.notification_service.common.constant;

import org.springframework.stereotype.Component;

@Component
public final class KafkaTopics {

    public static final String BRAND_DOCUMENTS_REQUESTED = "brand-documents-requested";
    public static final String BRAND_NEEDS_REVISION = "brand-needs-revision";
    public static final String BRAND_PROFILE_APPROVED = "brand-profile-approved";
    public static final String BRAND_PROFILE_REJECTED = "brand-profile-rejected";

    private KafkaTopics() {}
}
