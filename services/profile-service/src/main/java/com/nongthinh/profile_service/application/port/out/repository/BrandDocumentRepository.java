package com.nongthinh.profile_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.profile_service.domain.branddocument.BrandDocument;
import com.nongthinh.profile_service.domain.branddocument.valueobject.BrandDocumentReviewStatus;

public interface BrandDocumentRepository {

    BrandDocument save(BrandDocument brandDocument);

    Optional<BrandDocument> findById(UUID id);

    Optional<BrandDocument> findLatestByBrandProfileId(UUID brandProfileId);

    List<BrandDocument> findAllByBrandProfileIdOrderByCreatedAtDesc(UUID brandProfileId);

    List<BrandDocument> findAllByBrandProfileIdAndReviewStatus(
        UUID brandProfileId,
        BrandDocumentReviewStatus reviewStatus
    );
}
