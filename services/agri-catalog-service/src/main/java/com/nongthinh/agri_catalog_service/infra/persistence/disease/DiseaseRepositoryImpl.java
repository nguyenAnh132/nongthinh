package com.nongthinh.agri_catalog_service.infra.persistence.disease;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DiseaseRepositoryImpl implements DiseaseRepository {

    private final JpaDiseaseRepository jpaDiseaseRepository;
    private final DiseasePersistenceMapper diseasePersistenceMapper;

    @Override
    public boolean existsBySlugAndCropTypeId(String slug, UUID cropTypeId) {
        return jpaDiseaseRepository
                .existsBySlugIgnoreCaseAndCropTypeIdAndDeletedAtIsNull(slug, cropTypeId);
    }

    @Override
    public Optional<Disease> findById(UUID id) {
        return jpaDiseaseRepository.findByIdAndDeletedAtIsNull(id)
                .map(diseasePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Disease> findBySlugAndCropTypeId(String slug, UUID cropTypeId) {
        return jpaDiseaseRepository
                .findBySlugIgnoreCaseAndCropTypeIdAndDeletedAtIsNull(slug, cropTypeId)
                .map(diseasePersistenceMapper::toDomain);
    }

    @Override
    public List<Disease> findAll(UUID brandId, ReviewStatus reviewStatus, UUID cropTypeId) {
        return toDomains(jpaDiseaseRepository.findAllActive(
                brandId,
                reviewStatus == null ? null : reviewStatus.getValue(),
                cropTypeId
        ));
    }

    @Override
    public List<Disease> findAllApproved(UUID cropTypeId) {
        return toDomains(jpaDiseaseRepository.findAllApproved(cropTypeId));
    }

    @Override
    public boolean existsByCropTypeId(UUID cropTypeId) {
        return jpaDiseaseRepository.existsByCropTypeIdAndDeletedAtIsNull(cropTypeId);
    }

    @Override
    public List<Disease> findAllByBrandId(UUID brandId) {
        return toDomains(
                jpaDiseaseRepository
                        .findAllByBrandIdAndDeletedAtIsNullOrderByCreatedAtDesc(brandId)
        );
    }

    @Override
    public List<Disease> findAllByReviewStatus(ReviewStatus reviewStatus) {
        return toDomains(
                jpaDiseaseRepository
                        .findAllByReviewStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
                                reviewStatus.getValue()
                        )
        );
    }

    @Override
    public Disease save(Disease disease) {
        return diseasePersistenceMapper.toDomain(
                jpaDiseaseRepository.save(diseasePersistenceMapper.toEntity(disease))
        );
    }

    private List<Disease> toDomains(List<JpaDiseaseEntity> entities) {
        return entities.stream()
                .map(diseasePersistenceMapper::toDomain)
                .toList();
    }

}
