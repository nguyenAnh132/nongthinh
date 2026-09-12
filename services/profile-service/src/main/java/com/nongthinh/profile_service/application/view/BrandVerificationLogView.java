package com.nongthinh.profile_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.profile_service.domain.brandverificationlog.BrandVerificationLog;

public record BrandVerificationLogView(
        UUID id,
        UUID brandProfileId,
        UUID adminUserId,
        String phoneCalled,
        String result,
        String note,
        Instant verifiedAt,
        Instant createdAt
) {
    public static BrandVerificationLogView from(BrandVerificationLog log) {
        return new BrandVerificationLogView(
                log.getId(),
                log.getBrandProfileId(),
                log.getAdminUserId(),
                log.getPhoneCalled(),
                log.getResult().getValue(),
                log.getNote(),
                log.getVerifiedAt(),
                log.getCreatedAt()
        );
    }
}
