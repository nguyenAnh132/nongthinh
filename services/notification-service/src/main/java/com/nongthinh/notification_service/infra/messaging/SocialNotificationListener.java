package com.nongthinh.notification_service.infra.messaging;
import com.nongthinh.notification_service.application.event.SocialNotificationEvent;
import com.nongthinh.notification_service.application.port.in.notification.HandleSocialNotificationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
@Component
@RequiredArgsConstructor
public class SocialNotificationListener {
    private final ObjectMapper json;
    private final HandleSocialNotificationUseCase handler;
    @KafkaListener(topics="${PROFILE_NOTIFICATION_TOPIC:profile.notifications.v1}",
            groupId="social-notifications", containerFactory="postEngagementKafkaListenerContainerFactory",
            properties={"auto.offset.reset=earliest"})
    public void consume(String value) { handler.execute(json.readValue(value, SocialNotificationEvent.class)); }
}
