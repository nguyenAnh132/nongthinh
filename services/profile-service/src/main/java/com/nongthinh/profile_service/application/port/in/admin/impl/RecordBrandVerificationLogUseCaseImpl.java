package com.nongthinh.profile_service.application.port.in.admin.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.profile_service.application.command.admin.RecordBrandVerificationLogCommand;
import com.nongthinh.profile_service.application.port.in.admin.RecordBrandVerificationLogUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.IdGenerator;
import com.nongthinh.profile_service.application.port.out.repository.BrandLifecycleLogRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandVerificationLogRepository;
import com.nongthinh.profile_service.application.view.BrandVerificationLogView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.brandlifecyclelog.BrandLifecycleLog;
import com.nongthinh.profile_service.domain.brandlifecyclelog.valueobject.BrandLifecycleAction;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.brandverificationlog.BrandVerificationLog;
import com.nongthinh.profile_service.domain.brandverificationlog.valueobject.VerificationResult;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecordBrandVerificationLogUseCaseImpl implements RecordBrandVerificationLogUseCase {

    private final BrandProfileRepository brandProfileRepository;
    private final BrandVerificationLogRepository brandVerificationLogRepository;
    private final BrandLifecycleLogRepository brandLifecycleLogRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public BrandVerificationLogView execute(RecordBrandVerificationLogCommand command) {
        BrandProfile profile = brandProfileRepository.findById(command.brandProfileId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        VerificationResult result = VerificationResult.fromString(command.result());
        Instant now = clockProvider.now();

        BrandVerificationLog verificationLog = BrandVerificationLog.create(
                idGenerator.generate(),
                command.brandProfileId(),
                command.adminUserId(),
                command.phoneCalled(),
                result,
                command.note(),
                now,
                now
        );

        BrandVerificationLog saved = brandVerificationLogRepository.save(verificationLog);
        recordLifecycle(command.brandProfileId(), command.adminUserId(), profile, result, now);

        return BrandVerificationLogView.from(saved);
    }

    private void recordLifecycle(
            UUID brandProfileId,
            UUID adminUserId,
            BrandProfile profile,
            VerificationResult result,
            Instant now
    ) {
        BrandLifecycleLog lifecycleLog = BrandLifecycleLog.record(
                idGenerator.generate(),
                brandProfileId,
                BrandLifecycleAction.VERIFICATION_RECORDED,
                adminUserId,
                profile.getStatus(),
                profile.getStatus(),
                "{\"result\":\"" + result.getValue() + "\"}",
                now
        );
        brandLifecycleLogRepository.save(lifecycleLog);
    }
}
