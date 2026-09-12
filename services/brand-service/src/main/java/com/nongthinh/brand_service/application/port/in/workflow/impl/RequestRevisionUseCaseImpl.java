package com.nongthinh.brand_service.application.port.in.workflow.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.brand_service.application.event.BrandNeedsRevisionEvent;
import com.nongthinh.brand_service.application.port.in.workflow.RequestRevisionUseCase;
import com.nongthinh.brand_service.application.port.out.ClockProvider;
import com.nongthinh.brand_service.application.port.out.EventPublisher;
import com.nongthinh.brand_service.application.port.out.IdGenerator;
import com.nongthinh.brand_service.application.port.out.ProfileServicePort;
import com.nongthinh.brand_service.infra.client.profileservice.BrandProfileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestRevisionUseCaseImpl implements RequestRevisionUseCase {

    private final ProfileServicePort profileServicePort;
    private final EventPublisher eventPublisher;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public void execute(UUID brandProfileId, UUID actorUserId, String revisionReason) {
        BrandProfileDto profile = profileServicePort.updateBrandProfileStatus(
                brandProfileId,
                "NEEDS_REVISION",
                revisionReason,
                actorUserId
        );
        Instant now = clockProvider.now();

        eventPublisher.publish(new BrandNeedsRevisionEvent(
                idGenerator.generate(),
                now,
                profile.id(),
                profile.userId(),
                profile.brandName(),
                profile.representativeName(),
                profile.representativeEmail(),
                revisionReason,
                actorUserId
        ));

        log.info(
                "Requested brand revision: brandProfileId={}, actorUserId={}, reason={}",
                brandProfileId,
                actorUserId,
                revisionReason
        );
    }
}
