package com.nongthinh.notification_service.infra.messaging;

import com.nongthinh.notification_service.application.command.SendTemplatedEmailCommand;
import com.nongthinh.notification_service.application.event.BrandProfileApprovedEvent;
import com.nongthinh.notification_service.application.port.in.sendemail.SendTemplatedEmailUseCase;
import com.nongthinh.notification_service.application.port.out.EventDeserializer;
import com.nongthinh.notification_service.common.constant.EmailPurposeConstant;
import com.nongthinh.notification_service.common.constant.KafkaTopics;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrandProfileApprovedEventListener {

    private final EventDeserializer eventDeserializer;
    private final SendTemplatedEmailUseCase sendTemplatedEmailUseCase;

    @KafkaListener(topics = KafkaTopics.BRAND_PROFILE_APPROVED)
    public void consume(String message) {
        BrandProfileApprovedEvent event = eventDeserializer.deserialize(message, BrandProfileApprovedEvent.class);

        log.info(
                "Received BrandProfileApprovedEvent: eventId={}, brandProfileId={}, email={}",
                event.eventId(),
                event.brandProfileId(),
                event.representativeEmail()
        );

        sendTemplatedEmailUseCase.execute(new SendTemplatedEmailCommand(
                EmailPurposeConstant.BRAND_APPROVED,
                event.userId(),
                event.representativeEmail(),
                Map.of(
                        "brand_name", event.brandName(),
                        "user_name", event.representativeName()
                )
        ));
    }
}
