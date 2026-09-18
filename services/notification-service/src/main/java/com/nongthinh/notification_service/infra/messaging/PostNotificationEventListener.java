package com.nongthinh.notification_service.infra.messaging;
import com.nongthinh.notification_service.application.port.in.notification.HandlePostEngagementUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "in-app-notification.enabled", havingValue = "true")
public class PostNotificationEventListener {
    private final PostEngagementEventDecoder decoder;
    private final HandlePostEngagementUseCase useCase;
    @KafkaListener(topics = "#{@kafkaTopicProperties.postEngagement}",
            groupId = "notification-post-engagement-v1", containerFactory = "postEngagementKafkaListenerContainerFactory",
            properties = {"auto.offset.reset=earliest"})
    public void receive(String value) { useCase.execute(decoder.decode(value)); }
}
