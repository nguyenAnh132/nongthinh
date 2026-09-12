package com.nongthinh.profile_service.application.port.in.admin.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.profile_service.application.port.in.admin.RecordBrandDocumentsRequestedUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.IdGenerator;
import com.nongthinh.profile_service.application.port.out.repository.BrandLifecycleLogRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.brandlifecyclelog.BrandLifecycleLog;
import com.nongthinh.profile_service.domain.brandlifecyclelog.valueobject.BrandLifecycleAction;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecordBrandDocumentsRequestedUseCaseImpl implements RecordBrandDocumentsRequestedUseCase {

    private final BrandProfileRepository brandProfileRepository;
    private final BrandLifecycleLogRepository brandLifecycleLogRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public void execute(UUID brandProfileId, UUID actorUserId) {
        BrandProfile profile = brandProfileRepository.findById(brandProfileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        Instant now = clockProvider.now();
        BrandLifecycleLog lifecycleLog = BrandLifecycleLog.record(
                idGenerator.generate(),
                brandProfileId,
                BrandLifecycleAction.DOCUMENTS_REQUESTED,
                actorUserId,
                profile.getStatus(),
                profile.getStatus(),
                null,
                now
        );
        brandLifecycleLogRepository.save(lifecycleLog);
    }
}
