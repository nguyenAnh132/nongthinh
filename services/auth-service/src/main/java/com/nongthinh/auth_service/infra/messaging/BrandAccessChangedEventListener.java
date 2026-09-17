package com.nongthinh.auth_service.infra.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongthinh.auth_service.application.event.BrandAccessChangedEvent;
import com.nongthinh.auth_service.application.port.in.user.SynchronizeBrandRoleUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BrandAccessChangedEventListener {
    private final ObjectMapper mapper;
    private final SynchronizeBrandRoleUseCase synchronizeBrandRoleUseCase;

    public BrandAccessChangedEventListener(@Qualifier("eventObjectMapper") ObjectMapper mapper,
            SynchronizeBrandRoleUseCase synchronizeBrandRoleUseCase) {
        this.mapper = mapper;
        this.synchronizeBrandRoleUseCase = synchronizeBrandRoleUseCase;
    }

    @KafkaListener(topics = "${messaging.kafka.topics.brand-access-changed}",
            groupId = "auth-brand-access", containerFactory = "brandAccessKafkaListenerContainerFactory")
    public void onMessage(String payload) {
        BrandAccessChangedEvent event;
        try {
            event = mapper.readValue(payload, BrandAccessChangedEvent.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid brand access event", ex);
        }
        if (event == null || event.userId() == null || event.eventId() == null) {
            throw new IllegalArgumentException("Brand access event identifiers are required");
        }
        synchronizeBrandRoleUseCase.execute(event.userId());
    }
}
