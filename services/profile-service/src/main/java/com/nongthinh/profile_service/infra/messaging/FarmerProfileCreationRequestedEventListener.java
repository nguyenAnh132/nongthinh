package com.nongthinh.profile_service.infra.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.nongthinh.profile_service.application.event.FarmerProfileCreationRequestedEvent;
import com.nongthinh.profile_service.application.port.in.farmer.HandleFarmerProfileCreationRequestedUseCase;
import com.nongthinh.profile_service.application.port.out.EventDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FarmerProfileCreationRequestedEventListener {

    private final EventDeserializer eventDeserializer;
    private final HandleFarmerProfileCreationRequestedUseCase handleFarmerProfileCreationRequestedUseCase;

    @KafkaListener(topics = "#{@kafkaTopicProperties.farmerProfileCreationRequested}")
    public void consume(String message) {
        FarmerProfileCreationRequestedEvent event =
                eventDeserializer.deserialize(message, FarmerProfileCreationRequestedEvent.class);
        log.info("Received FarmerProfileCreationRequested event: eventId={}, userId={}",
                event.eventId(), event.userId());

        handleFarmerProfileCreationRequestedUseCase.execute(event);
    }
}
