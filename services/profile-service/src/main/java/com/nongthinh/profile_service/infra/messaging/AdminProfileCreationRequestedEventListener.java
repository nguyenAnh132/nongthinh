package com.nongthinh.profile_service.infra.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.nongthinh.profile_service.application.event.AdminProfileCreationRequestedEvent;
import com.nongthinh.profile_service.application.port.in.admin.HandleAdminProfileCreationRequestedUseCase;
import com.nongthinh.profile_service.application.port.out.EventDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminProfileCreationRequestedEventListener {

    private final EventDeserializer eventDeserializer;
    private final HandleAdminProfileCreationRequestedUseCase handleAdminProfileCreationRequestedUseCase;

    @KafkaListener(topics = "#{@kafkaTopicProperties.adminProfileCreationRequested}")
    public void consume(String message) {
        AdminProfileCreationRequestedEvent event =
                eventDeserializer.deserialize(message, AdminProfileCreationRequestedEvent.class);
        log.info("Received AdminProfileCreationRequested event: eventId={}, userId={}",
                event.eventId(), event.userId());

        handleAdminProfileCreationRequestedUseCase.execute(event);
    }
}
