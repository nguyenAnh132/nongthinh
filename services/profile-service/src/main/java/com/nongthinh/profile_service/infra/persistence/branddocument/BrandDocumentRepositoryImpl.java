package com.nongthinh.profile_service.infra.persistence.branddocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.profile_service.application.port.out.repository.BrandDocumentRepository;
import com.nongthinh.profile_service.domain.branddocument.BrandDocument;
import com.nongthinh.profile_service.domain.branddocument.valueobject.BrandDocumentReviewStatus;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BrandDocumentRepositoryImpl implements BrandDocumentRepository {

    private final JpaBrandDocumentRepository jpaBrandDocumentRepository;
    private final BrandDocumentPersistenceMapper brandDocumentPersistenceMapper;

    @Override
    public BrandDocument save(BrandDocument brandDocument) {
        JpaBrandDocumentEntity entity = brandDocumentPersistenceMapper.toEntity(brandDocument);
        JpaBrandDocumentEntity saved = jpaBrandDocumentRepository.save(entity);
        return brandDocumentPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<BrandDocument> findById(UUID id) {
        return jpaBrandDocumentRepository.findById(id)
                .map(brandDocumentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<BrandDocument> findLatestByBrandProfileId(UUID brandProfileId) {
        return jpaBrandDocumentRepository.findFirstByBrandProfileIdOrderByCreatedAtDesc(brandProfileId)
                .map(brandDocumentPersistenceMapper::toDomain);
    }

    @Override
    public List<BrandDocument> findAllByBrandProfileIdOrderByCreatedAtDesc(UUID brandProfileId) {
        return jpaBrandDocumentRepository.findAllByBrandProfileIdOrderByCreatedAtDesc(brandProfileId)
                .stream()
                .map(brandDocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<BrandDocument> findAllByBrandProfileIdAndReviewStatus(
        UUID brandProfileId,
        BrandDocumentReviewStatus reviewStatus
    ) {
        return jpaBrandDocumentRepository.findAllByBrandProfileIdAndReviewStatusOrderByCreatedAtDesc(
                brandProfileId,
                reviewStatus.getValue()
            )
            .stream()
            .map(brandDocumentPersistenceMapper::toDomain)
            .toList();
    }
}
