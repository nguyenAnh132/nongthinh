package com.nongthinh.brand_service.infra.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.nongthinh.brand_service.application.command.StartBrandApprovalProcessCommand;
import com.nongthinh.brand_service.application.event.BrandProfileCreatedEvent;
import com.nongthinh.brand_service.application.port.in.workflow.StartBrandApprovalProcessUseCase;
import com.nongthinh.brand_service.application.port.out.EventDeserializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrandProfileCreatedEventListener {

    private final EventDeserializer eventDeserializer;
    private final StartBrandApprovalProcessUseCase startBrandApprovalProcessUseCase;

    @KafkaListener(topics = "#{@kafkaTopicProperties.brandProfileCreated}")
    public void onBrandProfileCreated(String payload) {
        BrandProfileCreatedEvent event = eventDeserializer.deserialize(payload, BrandProfileCreatedEvent.class);

        log.info(
                "Received BrandProfileCreatedEvent: eventId={}, brandProfileId={}, userId={}",
                event.eventId(),
                event.brandProfileId(),
                event.userId()
        );

        startBrandApprovalProcessUseCase.execute(new StartBrandApprovalProcessCommand(
                event.brandProfileId(),
                event.userId(),
                event.brandName()
        ));
    }
}
