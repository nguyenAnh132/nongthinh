package com.nongthinh.profile_service.common.constant;

public final class KafkaTopicConstant {

    public static final String FARMER_PROFILE_CREATION_REQUESTED = "farmer-profile-creation-requested";
    public static final String ADMIN_PROFILE_CREATION_REQUESTED = "admin-profile-creation-requested";
    public static final String BRAND_PROFILE_CREATION_REQUESTED = "brand-profile-creation-requested";

    public static final String BRAND_PROFILE_CREATED = "brand-profile-created";
    public static final String BRAND_DOCUMENTS_SUBMITTED = "brand-documents-submitted";
    public static final String BRAND_PROFILE_REJECTED = "brand-profile-rejected";

    private KafkaTopicConstant() {}
}
