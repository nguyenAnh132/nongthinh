package com.nongthinh.profile_service.application.port.in.brand.impl;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.profile_service.application.event.BrandDocumentsSubmittedEvent;
import com.nongthinh.profile_service.application.port.in.brand.SubmitMyBrandDocumentsUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.EventPublisher;
import com.nongthinh.profile_service.application.port.out.IdGenerator;
import com.nongthinh.profile_service.application.port.out.repository.BrandDocumentRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandLifecycleLogRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
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
public class SubmitMyBrandDocumentsUseCaseImpl implements SubmitMyBrandDocumentsUseCase {

    private static final Set<BrandProfileStatus> ALLOWED_STATUSES = Set.of(
            BrandProfileStatus.UNDER_REVIEW,
            BrandProfileStatus.NEEDS_REVISION
    );

    private final CurrentUserProvider currentUserProvider;
    private final BrandProfileRepository brandProfileRepository;
    private final BrandDocumentRepository brandDocumentRepository;
    private final BrandLifecycleLogRepository brandLifecycleLogRepository;
    private final EventPublisher eventPublisher;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public void execute() {
        UUID userId = currentUserProvider.getCurrentUser().getUserId();
        BrandProfile profile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        if (!ALLOWED_STATUSES.contains(profile.getStatus())) {
            throw new BusinessException(ErrorCode.PROFILE_STATUS_INVALID);
        }

        BrandDocument document = brandDocumentRepository.findLatestByBrandProfileId(profile.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BRAND_DOCUMENT_NOT_FOUND));

        if (document.getReviewStatus() != BrandDocumentReviewStatus.PENDING_REVIEW) {
            throw new BusinessException(ErrorCode.BRAND_DOCUMENT_REVIEW_STATUS_INVALID);
        }

        Instant now = clockProvider.now();
        BrandProfileStatus fromStatus = profile.getStatus();

        if (profile.getStatus() == BrandProfileStatus.NEEDS_REVISION) {
            profile.markReadyForFinalReview(now);
            brandProfileRepository.save(profile);
        }

        brandLifecycleLogRepository.save(BrandLifecycleLog.record(
                idGenerator.generate(),
                profile.getId(),
                BrandLifecycleAction.DOCUMENTS_SUBMITTED,
                userId,
                fromStatus,
                profile.getStatus(),
                null,
                now
        ));

        eventPublisher.publish(new BrandDocumentsSubmittedEvent(
                idGenerator.generate(),
                now,
                profile.getId(),
                profile.getUserId(),
                profile.getBrandName().getValue()
        ));
    }
}
