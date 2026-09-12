package com.nongthinh.brand_service.application.port.in.workflow.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.brand_service.application.event.BrandProfileApprovedEvent;
import com.nongthinh.brand_service.application.port.in.workflow.ActivateBrandUseCase;
import com.nongthinh.brand_service.application.port.in.workflow.support.BrandApprovalProcessSupport;
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
public class ActivateBrandUseCaseImpl implements ActivateBrandUseCase {

    private final ProfileServicePort profileServicePort;
    private final BrandApprovalProcessSupport brandApprovalProcessSupport;
    private final EventPublisher eventPublisher;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public void execute(UUID brandProfileId, UUID actorUserId) {
        BrandProfileDto profile = profileServicePort.updateBrandProfileStatus(
                brandProfileId,
                "ACTIVE",
                null,
                actorUserId
        );
        brandApprovalProcessSupport.completeIfPresent(brandProfileId, actorUserId);

        Instant now = clockProvider.now();
        eventPublisher.publish(new BrandProfileApprovedEvent(
                idGenerator.generate(),
                now,
                profile.id(),
                profile.userId(),
                profile.brandName(),
                profile.representativeName(),
                profile.representativeEmail(),
                actorUserId
        ));

        log.info("Activated brand profile: brandProfileId={}, actorUserId={}", brandProfileId, actorUserId);
    }
}
