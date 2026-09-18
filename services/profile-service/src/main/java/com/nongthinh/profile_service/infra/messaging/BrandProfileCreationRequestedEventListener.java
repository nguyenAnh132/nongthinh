package com.nongthinh.profile_service.infra.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.nongthinh.profile_service.application.event.BrandProfileCreationRequestedEvent;
import com.nongthinh.profile_service.application.port.in.brand.HandleBrandProfileCreationRequestedUseCase;
import com.nongthinh.profile_service.application.port.out.EventDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrandProfileCreationRequestedEventListener {

    private final EventDeserializer eventDeserializer;
    private final HandleBrandProfileCreationRequestedUseCase handleBrandProfileCreationRequestedUseCase;

    @KafkaListener(topics = "#{@kafkaTopicProperties.brandProfileCreationRequested}")
    public void consume(String message) {
        BrandProfileCreationRequestedEvent event =
                eventDeserializer.deserialize(message, BrandProfileCreationRequestedEvent.class);
        log.info("Received BrandProfileCreationRequested event: eventId={}, userId={}",
                event.eventId(), event.userId());

        handleBrandProfileCreationRequestedUseCase.execute(event);
    }
}
