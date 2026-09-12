package com.nongthinh.profile_service.infra.persistence.branddocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaBrandDocumentRepository extends JpaRepository<JpaBrandDocumentEntity, UUID> {

    Optional<JpaBrandDocumentEntity> findFirstByBrandProfileIdOrderByCreatedAtDesc(UUID brandProfileId);

    List<JpaBrandDocumentEntity> findAllByBrandProfileIdOrderByCreatedAtDesc(UUID brandProfileId);

    List<JpaBrandDocumentEntity> findAllByBrandProfileIdAndReviewStatusOrderByCreatedAtDesc(
        UUID brandProfileId,
        String reviewStatus
    );
}
