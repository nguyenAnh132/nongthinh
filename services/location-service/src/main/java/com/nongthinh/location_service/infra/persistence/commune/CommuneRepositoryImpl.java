package com.nongthinh.location_service.infra.persistence.commune;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.location_service.application.port.out.repository.CommuneRepository;
import com.nongthinh.location_service.domain.commune.Commune;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommuneRepositoryImpl implements CommuneRepository {

    private final JpaCommuneRepository jpaCommuneRepository;
    private final CommunePersistenceMapper communePersistenceMapper;

    @Override
    public boolean existsById(UUID id) {
        return jpaCommuneRepository.existsById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaCommuneRepository.existsByCode(code);
    }

    @Override
    public boolean existsByProvinceId(String provinceId) {
        return jpaCommuneRepository.existsByProvinceId(provinceId);
    }

    @Override
    public Optional<Commune> findById(UUID id) {
        return jpaCommuneRepository.findById(id)
                .map(communePersistenceMapper::toDomain);
    }

    @Override
    public List<Commune> findByProvinceIdOrderByCode(String provinceId) {
        return jpaCommuneRepository.findByProvinceIdOrderByCodeAsc(provinceId)
                .stream()
                .map(communePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Commune save(Commune commune) {
        JpaCommuneEntity entity = communePersistenceMapper.toEntity(commune);
        JpaCommuneEntity saved = jpaCommuneRepository.save(entity);
        return communePersistenceMapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaCommuneRepository.deleteById(id);
    }
}
