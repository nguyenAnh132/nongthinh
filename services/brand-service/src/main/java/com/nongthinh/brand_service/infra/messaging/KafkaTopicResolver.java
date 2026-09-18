package com.nongthinh.brand_service.infra.messaging;

import com.nongthinh.brand_service.application.event.BrandDocumentsRequestedEvent;
import com.nongthinh.brand_service.application.event.BrandNeedsRevisionEvent;
import com.nongthinh.brand_service.application.event.BrandProfileApprovedEvent;
import com.nongthinh.brand_service.application.event.BrandProfileRejectedEvent;
import com.nongthinh.brand_service.application.event.DomainEvent;
import com.nongthinh.brand_service.configuration.KafkaTopicProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaTopicResolver {

    private final KafkaTopicProperties topics;

    public String resolve(DomainEvent event) {
        if (event instanceof BrandDocumentsRequestedEvent) {
            return topics.getBrandDocumentsRequested();
        }
        if (event instanceof BrandNeedsRevisionEvent) {
            return topics.getBrandNeedsRevision();
        }
        if (event instanceof BrandProfileApprovedEvent) {
            return topics.getBrandProfileApproved();
        }
        if (event instanceof BrandProfileRejectedEvent) {
            return topics.getBrandProfileRejected();
        }
        throw new IllegalArgumentException("Unknown event: " + event.getClass().getSimpleName());
    }
}
