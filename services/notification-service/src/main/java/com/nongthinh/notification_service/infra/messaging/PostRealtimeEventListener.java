package com.nongthinh.notification_service.infra.messaging;
import com.nongthinh.notification_service.infra.realtime.SseConnectionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "post-realtime.enabled", havingValue = "true")
public class PostRealtimeEventListener {
    private final PostEngagementEventDecoder decoder;
    private final SseConnectionRegistry registry;
    @KafkaListener(topics = "${post-realtime.topic:post.engagement.v1}",
            groupId = "post-sse-${post-realtime.instance-id:${random.uuid}}",
            containerFactory = "postEngagementKafkaListenerContainerFactory", properties = {"auto.offset.reset=latest"})
    public void receive(String value) { registry.broadcast(decoder.decode(value)); }
}
