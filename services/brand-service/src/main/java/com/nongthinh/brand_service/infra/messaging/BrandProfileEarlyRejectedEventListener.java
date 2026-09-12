package com.nongthinh.brand_service.infra.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.nongthinh.brand_service.application.event.BrandProfileRejectedEvent;
import com.nongthinh.brand_service.application.port.in.workflow.CancelBrandApprovalProcessUseCase;
import com.nongthinh.brand_service.application.port.out.EventDeserializer;
import com.nongthinh.brand_service.common.constant.KafkaTopicConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrandProfileEarlyRejectedEventListener {

    private final EventDeserializer eventDeserializer;
    private final CancelBrandApprovalProcessUseCase cancelBrandApprovalProcessUseCase;

    @KafkaListener(topics = KafkaTopicConstant.BRAND_PROFILE_REJECTED)
    public void onBrandProfileRejected(String payload) {
        BrandProfileRejectedEvent event = eventDeserializer.deserialize(payload, BrandProfileRejectedEvent.class);

        if (!event.earlyReject()) {
            return;
        }

        log.info(
                "Received early BrandProfileRejectedEvent: eventId={}, brandProfileId={}",
                event.eventId(),
                event.brandProfileId()
        );

        cancelBrandApprovalProcessUseCase.execute(
                event.brandProfileId(),
                event.actorUserId(),
                event.rejectionReason()
        );
    }
}
