package com.nongthinh.brand_service.infra.messaging;

import com.nongthinh.brand_service.application.event.BrandDocumentsRequestedEvent;
import com.nongthinh.brand_service.application.event.BrandNeedsRevisionEvent;
import com.nongthinh.brand_service.application.event.BrandProfileApprovedEvent;
import com.nongthinh.brand_service.application.event.BrandProfileRejectedEvent;
import com.nongthinh.brand_service.application.event.DomainEvent;
import com.nongthinh.brand_service.common.constant.KafkaTopicConstant;

public final class KafkaTopicResolver {

    public static String resolve(DomainEvent event) {
        if (event instanceof BrandDocumentsRequestedEvent) {
            return KafkaTopicConstant.BRAND_DOCUMENTS_REQUESTED;
        }
        if (event instanceof BrandNeedsRevisionEvent) {
            return KafkaTopicConstant.BRAND_NEEDS_REVISION;
        }
        if (event instanceof BrandProfileApprovedEvent) {
            return KafkaTopicConstant.BRAND_PROFILE_APPROVED;
        }
        if (event instanceof BrandProfileRejectedEvent) {
            return KafkaTopicConstant.BRAND_PROFILE_REJECTED;
        }
        throw new IllegalArgumentException("Unknown event: " + event.getClass().getSimpleName());
    }

    private KafkaTopicResolver() {
    }
}
