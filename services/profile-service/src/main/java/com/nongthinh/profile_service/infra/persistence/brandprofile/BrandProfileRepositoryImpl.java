package com.nongthinh.profile_service.infra.persistence.brandprofile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BrandProfileRepositoryImpl implements BrandProfileRepository {

    private final JpaBrandProfileRepository jpaBrandProfileRepository;
    private final BrandProfilePersistenceMapper brandProfilePersistenceMapper;

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaBrandProfileRepository.existsByUserId(userId);
    }

    @Override
    public Optional<BrandProfile> findById(UUID id) {
        return jpaBrandProfileRepository.findById(id)
                .map(brandProfilePersistenceMapper::toDomain);
    }

    @Override
    public Optional<BrandProfile> findByUserId(UUID userId) {
        return jpaBrandProfileRepository.findByUserId(userId)
                .map(brandProfilePersistenceMapper::toDomain);
    }

    @Override
    public List<BrandProfile> findAllByStatusIn(Collection<String> statuses) {
        return jpaBrandProfileRepository.findAllByStatusInOrderByCreatedAtDesc(statuses)
                .stream()
                .map(brandProfilePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public BrandProfile save(BrandProfile brandProfile) {
        JpaBrandProfileEntity entity = brandProfilePersistenceMapper.toEntity(brandProfile);
        JpaBrandProfileEntity saved = jpaBrandProfileRepository.save(entity);
        return brandProfilePersistenceMapper.toDomain(saved);
    }
}
