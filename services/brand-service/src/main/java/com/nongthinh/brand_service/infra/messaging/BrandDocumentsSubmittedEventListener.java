package com.nongthinh.brand_service.infra.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.nongthinh.brand_service.application.event.BrandDocumentsSubmittedEvent;
import com.nongthinh.brand_service.application.port.in.workflow.CorrelateDocumentsSubmittedUseCase;
import com.nongthinh.brand_service.application.port.out.EventDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrandDocumentsSubmittedEventListener {

    private final EventDeserializer eventDeserializer;
    private final CorrelateDocumentsSubmittedUseCase correlateDocumentsSubmittedUseCase;

    @KafkaListener(topics = "#{@kafkaTopicProperties.brandDocumentsSubmitted}")
    public void onDocumentsSubmitted(String payload) {
        BrandDocumentsSubmittedEvent event = eventDeserializer.deserialize(payload, BrandDocumentsSubmittedEvent.class);

        log.info(
                "Received BrandDocumentsSubmittedEvent: eventId={}, brandProfileId={}",
                event.eventId(),
                event.brandProfileId()
        );

        correlateDocumentsSubmittedUseCase.execute(event.brandProfileId());
    }
}
