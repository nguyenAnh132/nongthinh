package com.nongthinh.profile_service.infra.messaging;

import com.nongthinh.profile_service.application.event.BrandDocumentsSubmittedEvent;
import com.nongthinh.profile_service.application.event.BrandProfileCreatedEvent;
import com.nongthinh.profile_service.application.event.BrandProfileRejectedEvent;
import com.nongthinh.profile_service.application.event.DomainEvent;
import com.nongthinh.profile_service.configuration.KafkaTopicProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaTopicResolver {

    private final KafkaTopicProperties topics;

    public String resolve(DomainEvent event) {
        if (event instanceof BrandProfileCreatedEvent) {
            return topics.getBrandProfileCreated();
        }
        if (event instanceof BrandDocumentsSubmittedEvent) {
            return topics.getBrandDocumentsSubmitted();
        }
        if (event instanceof BrandProfileRejectedEvent) {
            return topics.getBrandProfileRejected();
        }
        throw new IllegalArgumentException("Unknown event: " + event.getClass().getSimpleName());
    }
}
