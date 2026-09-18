package com.nongthinh.auth_service.infra.messaging;

import com.nongthinh.auth_service.application.event.AdminProfileCreationRequestedEvent;
import com.nongthinh.auth_service.application.event.BrandProfileCreationRequestedEvent;
import com.nongthinh.auth_service.application.event.DomainEvent;
import com.nongthinh.auth_service.application.event.FarmerProfileCreationRequestedEvent;
import com.nongthinh.auth_service.configuration.property.KafkaTopicProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaTopicResolver {

    private final KafkaTopicProperties topics;

    public String resolve(DomainEvent event) {
        if (event instanceof FarmerProfileCreationRequestedEvent) {
            return topics.farmerProfileCreationRequested();
        }
        if (event instanceof AdminProfileCreationRequestedEvent) {
            return topics.adminProfileCreationRequested();
        }
        if (event instanceof BrandProfileCreationRequestedEvent) {
            return topics.brandProfileCreationRequested();
        }
        throw new IllegalArgumentException("Unknown event: ");
    }
}
