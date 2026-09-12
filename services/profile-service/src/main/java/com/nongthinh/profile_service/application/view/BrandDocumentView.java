package com.nongthinh.profile_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.profile_service.domain.branddocument.BrandDocument;

public record BrandDocumentView(
        UUID id,
        UUID brandProfileId,
        String businessLicenseUrl,
        String reviewStatus,
        UUID reviewedBy,
        Instant reviewedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static BrandDocumentView from(BrandDocument document) {
        return new BrandDocumentView(
                document.getId(),
                document.getBrandProfileId(),
                document.getBusinessLicenseUrl(),
                document.getReviewStatus().getValue(),
                document.getReviewedBy(),
                document.getReviewedAt(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
