package com.nongthinh.profile_service.application.port.in.brand.impl;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.profile_service.application.port.in.brand.UploadMyBrandDocumentUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.IdGenerator;
import com.nongthinh.profile_service.application.port.out.repository.BrandDocumentRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandLifecycleLogRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.view.BrandDocumentView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.branddocument.BrandDocument;
import com.nongthinh.profile_service.domain.brandlifecyclelog.BrandLifecycleLog;
import com.nongthinh.profile_service.domain.brandlifecyclelog.valueobject.BrandLifecycleAction;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandProfileStatus;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadMyBrandDocumentUseCaseImpl implements UploadMyBrandDocumentUseCase {

    private static final Set<BrandProfileStatus> ALLOWED_STATUSES = Set.of(
            BrandProfileStatus.UNDER_REVIEW,
            BrandProfileStatus.NEEDS_REVISION
    );

    private final CurrentUserProvider currentUserProvider;
    private final BrandProfileRepository brandProfileRepository;
    private final BrandDocumentRepository brandDocumentRepository;
    private final BrandLifecycleLogRepository brandLifecycleLogRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public BrandDocumentView execute(String businessLicenseUrl) {
        if (businessLicenseUrl == null || businessLicenseUrl.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }

        UUID userId = currentUserProvider.getCurrentUser().getUserId();
        BrandProfile profile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        if (!ALLOWED_STATUSES.contains(profile.getStatus())) {
            throw new BusinessException(ErrorCode.PROFILE_STATUS_INVALID);
        }

        Instant now = clockProvider.now();
        BrandDocument document = brandDocumentRepository.findLatestByBrandProfileId(profile.getId())
                .map(existing -> {
                    existing.replaceBusinessLicense(businessLicenseUrl.trim(), now);
                    return existing;
                })
                .orElseGet(() -> BrandDocument.create(
                        idGenerator.generate(),
                        profile.getId(),
                        businessLicenseUrl.trim(),
                        now
                ));

        BrandDocument saved = brandDocumentRepository.save(document);

        brandLifecycleLogRepository.save(BrandLifecycleLog.record(
                idGenerator.generate(),
                profile.getId(),
                BrandLifecycleAction.DOCUMENT_UPLOADED,
                userId,
                profile.getStatus(),
                profile.getStatus(),
                "{\"businessLicenseUrl\":\"" + businessLicenseUrl.trim().replace("\"", "\\\"") + "\"}",
                now
        ));

        return BrandDocumentView.from(saved);
    }
}
