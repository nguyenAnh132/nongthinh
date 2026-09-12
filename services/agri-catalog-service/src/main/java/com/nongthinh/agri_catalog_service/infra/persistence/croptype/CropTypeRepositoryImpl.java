package com.nongthinh.agri_catalog_service.infra.persistence.croptype;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.domain.croptype.CropType;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CropTypeRepositoryImpl implements CropTypeRepository {

    private final JpaCropTypeRepository jpaCropTypeRepository;
    private final CropTypePersistenceMapper cropTypePersistenceMapper;

    @Override
    public boolean existsByCode(String code) {
        return jpaCropTypeRepository.existsByCodeIgnoreCase(code);
    }

    @Override
    public Optional<CropType> findById(UUID id) {
        return jpaCropTypeRepository.findByIdAndDeletedAtIsNull(id)
                .map(cropTypePersistenceMapper::toDomain);
    }

    @Override
    public List<CropType> findAll() {
        return toDomains(jpaCropTypeRepository.findAllByDeletedAtIsNullOrderByNameAsc());
    }

    @Override
    public List<CropType> findAllActive() {
        return toDomains(
                jpaCropTypeRepository.findAllByDeletedAtIsNullAndActiveTrueOrderByNameAsc()
        );
    }

    @Override
    public CropType save(CropType cropType) {
        return cropTypePersistenceMapper.toDomain(
                jpaCropTypeRepository.save(cropTypePersistenceMapper.toEntity(cropType))
        );
    }

    private List<CropType> toDomains(List<JpaCropTypeEntity> entities) {
        return entities.stream()
                .map(cropTypePersistenceMapper::toDomain)
                .toList();
    }
}

