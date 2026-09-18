package com.nongthinh.profile_service.infra.messaging;
import com.nongthinh.profile_service.application.event.PostPublishedEvent;
import com.nongthinh.profile_service.application.port.in.follow.HandleFollowedPostUseCase;
import com.nongthinh.profile_service.application.port.out.EventDeserializer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class FollowedPostListener {
    private final EventDeserializer decoder;
    private final HandleFollowedPostUseCase handler;
    @KafkaListener(topics="#{@kafkaTopicProperties.postEngagement}",
            groupId="profile-follow-notifications", containerFactory="followKafkaListenerContainerFactory",
            properties={"auto.offset.reset=earliest", "enable.auto.commit=false"})
    public void consume(String message) { handler.execute(decoder.deserialize(message, PostPublishedEvent.class)); }
}
