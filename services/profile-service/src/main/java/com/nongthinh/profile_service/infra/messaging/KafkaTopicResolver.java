package com.nongthinh.profile_service.infra.messaging;

import com.nongthinh.profile_service.application.event.BrandDocumentsSubmittedEvent;
import com.nongthinh.profile_service.application.event.BrandProfileCreatedEvent;
import com.nongthinh.profile_service.application.event.BrandProfileRejectedEvent;
import com.nongthinh.profile_service.application.event.DomainEvent;
import com.nongthinh.profile_service.common.constant.KafkaTopicConstant;

public final class KafkaTopicResolver {

    public static String resolve(DomainEvent event) {
        if (event instanceof BrandProfileCreatedEvent) {
            return KafkaTopicConstant.BRAND_PROFILE_CREATED;
        }
        if (event instanceof BrandDocumentsSubmittedEvent) {
            return KafkaTopicConstant.BRAND_DOCUMENTS_SUBMITTED;
        }
        if (event instanceof BrandProfileRejectedEvent) {
            return KafkaTopicConstant.BRAND_PROFILE_REJECTED;
        }
        throw new IllegalArgumentException("Unknown event: " + event.getClass().getSimpleName());
    }

    private KafkaTopicResolver() {
    }
}
