package com.nongthinh.notification_service.infra.messaging;

import com.nongthinh.notification_service.application.event.BrandAccessChangedEvent;
import com.nongthinh.notification_service.application.port.out.EventDeserializer;
import com.nongthinh.notification_service.infra.realtime.SseConnectionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrandAccessChangedEventListener {
    private final EventDeserializer deserializer;
    private final SseConnectionRegistry registry;

    @KafkaListener(topics = "${messaging.kafka.topics.brand-access-changed}",
            groupId = "brand-access-sse-${post-realtime.instance-id:${random.uuid}}",
            containerFactory = "postEngagementKafkaListenerContainerFactory",
            properties = {"auto.offset.reset=latest"})
    public void consume(String message) {
        var event = deserializer.deserialize(message, BrandAccessChangedEvent.class);
        if (event == null || event.userId() == null || event.eventId() == null) {
            throw new IllegalArgumentException("Brand access event identifiers are required");
        }
        // Every instance closes its local connections. A reconnect must pass the current access policy.
        registry.disconnect(event.userId());
    }
}
