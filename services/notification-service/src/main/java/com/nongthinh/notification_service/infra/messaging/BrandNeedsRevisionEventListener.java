package com.nongthinh.notification_service.infra.messaging;

import com.nongthinh.notification_service.application.command.SendTemplatedEmailCommand;
import com.nongthinh.notification_service.application.event.BrandNeedsRevisionEvent;
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
public class BrandNeedsRevisionEventListener {

    private final EventDeserializer eventDeserializer;
    private final SendTemplatedEmailUseCase sendTemplatedEmailUseCase;

    @KafkaListener(topics = KafkaTopics.BRAND_NEEDS_REVISION)
    public void consume(String message) {
        BrandNeedsRevisionEvent event = eventDeserializer.deserialize(message, BrandNeedsRevisionEvent.class);

        log.info(
                "Received BrandNeedsRevisionEvent: eventId={}, brandProfileId={}, email={}",
                event.eventId(),
                event.brandProfileId(),
                event.representativeEmail()
        );

        sendTemplatedEmailUseCase.execute(new SendTemplatedEmailCommand(
                EmailPurposeConstant.BRAND_NEEDS_REVISION,
                event.userId(),
                event.representativeEmail(),
                Map.of(
                        "brand_name", event.brandName(),
                        "user_name", event.representativeName(),
                        "revision_reason", event.revisionReason()
                )
        ));
    }
}
