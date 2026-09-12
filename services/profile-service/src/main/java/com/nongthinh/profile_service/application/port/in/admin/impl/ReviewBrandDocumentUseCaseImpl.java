package com.nongthinh.profile_service.application.port.in.admin.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.profile_service.application.command.admin.ReviewBrandDocumentCommand;
import com.nongthinh.profile_service.application.port.in.admin.ReviewBrandDocumentUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.IdGenerator;
import com.nongthinh.profile_service.application.port.out.repository.BrandDocumentRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandLifecycleLogRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.branddocument.BrandDocument;
import com.nongthinh.profile_service.domain.branddocument.valueobject.BrandDocumentReviewStatus;
import com.nongthinh.profile_service.domain.brandlifecyclelog.BrandLifecycleLog;
import com.nongthinh.profile_service.domain.brandlifecyclelog.valueobject.BrandLifecycleAction;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandProfileStatus;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewBrandDocumentUseCaseImpl implements ReviewBrandDocumentUseCase {

    private final BrandProfileRepository brandProfileRepository;
    private final BrandDocumentRepository brandDocumentRepository;
    private final BrandLifecycleLogRepository brandLifecycleLogRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public BrandProfileView execute(ReviewBrandDocumentCommand command) {
        BrandProfile profile = brandProfileRepository.findById(command.brandProfileId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        BrandDocument document = brandDocumentRepository.findLatestByBrandProfileId(command.brandProfileId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BRAND_DOCUMENT_NOT_FOUND));

        if (document.getReviewStatus() != BrandDocumentReviewStatus.PENDING_REVIEW) {
            throw new BusinessException(ErrorCode.BRAND_DOCUMENT_REVIEW_STATUS_INVALID);
        }

        Instant now = clockProvider.now();
        BrandProfileStatus fromStatus = profile.getStatus();

        if (command.documentsOk()) {
            document.approve(command.adminUserId(), now);
            brandDocumentRepository.save(document);
            profile.markReadyForFinalReview(now);
            brandProfileRepository.save(profile);
            if (fromStatus != profile.getStatus()) {
                recordStatusChange(command.brandProfileId(), command.adminUserId(), fromStatus, profile.getStatus(), now);
            }
        } else {
            requireRevisionReason(command.revisionReason());
            document.requestRevision(command.adminUserId(), now);
            brandDocumentRepository.save(document);
        }

        recordDocumentReview(
                command.brandProfileId(),
                command.adminUserId(),
                fromStatus,
                profile.getStatus(),
                command.documentsOk(),
                command.revisionReason(),
                now
        );

        return BrandProfileView.from(profile);
    }

    private void requireRevisionReason(String revisionReason) {
        if (revisionReason == null || revisionReason.isBlank()) {
            throw new BusinessException(ErrorCode.REVISION_REASON_REQUIRED);
        }
    }

    private void recordStatusChange(
            UUID brandProfileId,
            UUID actorUserId,
            BrandProfileStatus fromStatus,
            BrandProfileStatus toStatus,
            Instant now
    ) {
        BrandLifecycleLog lifecycleLog = BrandLifecycleLog.recordStatusChange(
                idGenerator.generate(),
                brandProfileId,
                actorUserId,
                fromStatus,
                toStatus,
                null,
                now
        );
        brandLifecycleLogRepository.save(lifecycleLog);
    }

    private void recordDocumentReview(
            UUID brandProfileId,
            UUID actorUserId,
            BrandProfileStatus fromStatus,
            BrandProfileStatus toStatus,
            boolean documentsOk,
            String revisionReason,
            Instant now
    ) {
        String payload = documentsOk
                ? "{\"documentsOk\":true}"
                : "{\"documentsOk\":false,\"revisionReason\":\"" + revisionReason.replace("\"", "\\\"") + "\"}";

        BrandLifecycleLog lifecycleLog = BrandLifecycleLog.record(
                idGenerator.generate(),
                brandProfileId,
                BrandLifecycleAction.DOCUMENT_REVIEWED,
                actorUserId,
                fromStatus,
                toStatus,
                payload,
                now
        );
        brandLifecycleLogRepository.save(lifecycleLog);
    }
}
