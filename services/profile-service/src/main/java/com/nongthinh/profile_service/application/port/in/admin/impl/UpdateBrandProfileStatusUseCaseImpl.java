package com.nongthinh.profile_service.application.port.in.admin.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.profile_service.application.port.in.admin.UpdateBrandProfileStatusUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.IdGenerator;
import com.nongthinh.profile_service.application.port.out.repository.BrandLifecycleLogRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.brandlifecyclelog.BrandLifecycleLog;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandProfileStatus;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import com.nongthinh.profile_service.presentation.dto.request.admin.BrandProfileStatusUpdateRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateBrandProfileStatusUseCaseImpl implements UpdateBrandProfileStatusUseCase {

    private final BrandProfileRepository brandProfileRepository;
    private final BrandLifecycleLogRepository brandLifecycleLogRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public BrandProfileView execute(UUID id, BrandProfileStatusUpdateRequest request) {
        BrandProfile profile = brandProfileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        Instant now = clockProvider.now();
        BrandProfileStatus fromStatus = profile.getStatus();
        BrandProfileStatus targetStatus = BrandProfileStatus.fromString(request.status());

        applyStatus(profile, targetStatus, request.rejectionReason(), request.actorUserId(), now);
        BrandProfile saved = brandProfileRepository.save(profile);

        if (fromStatus != saved.getStatus()) {
            recordStatusChange(id, request.actorUserId(), fromStatus, saved.getStatus(), request.rejectionReason(), now);
        }

        return BrandProfileView.from(saved);
    }

    private void applyStatus(
            BrandProfile profile,
            BrandProfileStatus status,
            String rejectionReason,
            UUID actorUserId,
            Instant now
    ) {
        switch (status) {
            case ACTIVE -> profile.approve(actorUserId, now);
            case REJECTED -> profile.reject(rejectionReason, actorUserId, now);
            case UNDER_REVIEW -> profile.startReview(now);
            case NEEDS_REVISION -> profile.requestRevision(rejectionReason, now);
            case READY_FOR_FINAL_REVIEW -> profile.markReadyForFinalReview(now);
            case LOCKED -> profile.lock(now);
            case DISABLED -> profile.disable(now);
            case DELETED -> profile.markDeleted(now);
            case PENDING_APPROVAL -> throw new BusinessException(ErrorCode.PROFILE_STATUS_INVALID);
        }
    }

    private void recordStatusChange(
            UUID brandProfileId,
            UUID actorUserId,
            BrandProfileStatus fromStatus,
            BrandProfileStatus toStatus,
            String rejectionReason,
            Instant now
    ) {
        String payload = rejectionReason == null || rejectionReason.isBlank()
                ? null
                : "{\"reason\":\"" + rejectionReason.replace("\"", "\\\"") + "\"}";

        BrandLifecycleLog lifecycleLog = BrandLifecycleLog.recordStatusChange(
                idGenerator.generate(),
                brandProfileId,
                actorUserId,
                fromStatus,
                toStatus,
                payload,
                now
        );
        brandLifecycleLogRepository.save(lifecycleLog);
    }
}
