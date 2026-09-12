package com.nongthinh.profile_service.application.port.in.admin.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.profile_service.application.event.BrandProfileRejectedEvent;
import com.nongthinh.profile_service.application.port.in.admin.RejectBrandEarlyUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.EventPublisher;
import com.nongthinh.profile_service.application.port.out.IdGenerator;
import com.nongthinh.profile_service.application.port.out.repository.BrandLifecycleLogRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.brandlifecyclelog.BrandLifecycleLog;
import com.nongthinh.profile_service.domain.brandlifecyclelog.valueobject.BrandLifecycleAction;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandProfileStatus;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RejectBrandEarlyUseCaseImpl implements RejectBrandEarlyUseCase {

    private final BrandProfileRepository brandProfileRepository;
    private final BrandLifecycleLogRepository brandLifecycleLogRepository;
    private final EventPublisher eventPublisher;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public BrandProfileView execute(UUID brandProfileId, UUID actorUserId, String rejectionReason) {
        BrandProfile profile = brandProfileRepository.findById(brandProfileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        if (profile.getStatus() == BrandProfileStatus.REJECTED
                || profile.getStatus() == BrandProfileStatus.ACTIVE
                || profile.getStatus() == BrandProfileStatus.DELETED) {
            throw new BusinessException(ErrorCode.PROFILE_STATUS_INVALID);
        }

        Instant now = clockProvider.now();
        BrandProfileStatus fromStatus = profile.getStatus();
        profile.reject(rejectionReason, actorUserId, now);
        BrandProfile saved = brandProfileRepository.save(profile);

        brandLifecycleLogRepository.save(BrandLifecycleLog.record(
                idGenerator.generate(),
                brandProfileId,
                BrandLifecycleAction.EARLY_REJECTED,
                actorUserId,
                fromStatus,
                saved.getStatus(),
                "{\"reason\":\"" + rejectionReason.replace("\"", "\\\"") + "\"}",
                now
        ));

        eventPublisher.publish(new BrandProfileRejectedEvent(
                idGenerator.generate(),
                now,
                saved.getId(),
                saved.getUserId(),
                saved.getBrandName().getValue(),
                saved.getRepresentativeName(),
                saved.getRepresentativeEmail(),
                saved.getRejectionReason(),
                saved.getScheduledDeletionAt(),
                actorUserId,
                true
        ));

        return BrandProfileView.from(saved);
    }
}
