package com.nongthinh.auth_service.infra.messaging;

import com.nongthinh.auth_service.application.event.AdminProfileCreationRequestedEvent;
import com.nongthinh.auth_service.application.event.BrandProfileCreationRequestedEvent;
import com.nongthinh.auth_service.application.event.DomainEvent;
import com.nongthinh.auth_service.application.event.FarmerProfileCreationRequestedEvent;
import com.nongthinh.auth_service.common.constant.KafkaTopicConstant;

public final class KafkaTopicResolver {

    public static String resolve(DomainEvent event) {
        if (event instanceof FarmerProfileCreationRequestedEvent) {
            return KafkaTopicConstant.FARMER_PROFILE_CREATION_REQUESTED;
        }
        if (event instanceof AdminProfileCreationRequestedEvent) {
            return KafkaTopicConstant.ADMIN_PROFILE_CREATION_REQUESTED;
        }
        if (event instanceof BrandProfileCreationRequestedEvent) {
            return KafkaTopicConstant.BRAND_PROFILE_CREATION_REQUESTED;
        }
        throw new IllegalArgumentException("Unknown event: ");
    }

    private KafkaTopicResolver() {
    }
}
