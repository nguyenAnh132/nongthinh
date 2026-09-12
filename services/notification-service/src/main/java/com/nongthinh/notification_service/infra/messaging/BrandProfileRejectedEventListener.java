package com.nongthinh.notification_service.infra.messaging;

import com.nongthinh.notification_service.application.command.SendTemplatedEmailCommand;
import com.nongthinh.notification_service.application.event.BrandProfileRejectedEvent;
import com.nongthinh.notification_service.application.port.in.sendemail.SendTemplatedEmailUseCase;
import com.nongthinh.notification_service.application.port.out.EventDeserializer;
import com.nongthinh.notification_service.common.constant.EmailPurposeConstant;
import com.nongthinh.notification_service.common.constant.KafkaTopics;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrandProfileRejectedEventListener {

    private final EventDeserializer eventDeserializer;
    private final SendTemplatedEmailUseCase sendTemplatedEmailUseCase;

    @KafkaListener(topics = KafkaTopics.BRAND_PROFILE_REJECTED)
    public void consume(String message) {
        BrandProfileRejectedEvent event = eventDeserializer.deserialize(message, BrandProfileRejectedEvent.class);

        log.info(
                "Received BrandProfileRejectedEvent: eventId={}, brandProfileId={}, email={}",
                event.eventId(),
                event.brandProfileId(),
                event.representativeEmail()
        );

        Map<String, Object> variables = new HashMap<>();
        variables.put("brand_name", event.brandName());
        variables.put("user_name", event.representativeName());
        variables.put("reject_reason", event.rejectionReason());
        if (event.canReRegisterAt() != null) {
            variables.put("can_re_register_at", event.canReRegisterAt().toString());
        }

        sendTemplatedEmailUseCase.execute(new SendTemplatedEmailCommand(
                EmailPurposeConstant.BRAND_REJECTED,
                event.userId(),
                event.representativeEmail(),
                variables
        ));
    }
}
