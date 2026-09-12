package com.nongthinh.profile_service.infra.persistence.farmerprofile;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.profile_service.application.port.out.repository.FarmerProfileRepository;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FarmerProfileRepositoryImpl implements FarmerProfileRepository {

    private final JpaFarmerProfileRepository jpaFarmerProfileRepository;
    private final FarmerProfilePersistenceMapper farmerProfilePersistenceMapper;

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaFarmerProfileRepository.existsByUserId(userId);
    }

    @Override
    public Optional<FarmerProfile> findById(UUID id) {
        return jpaFarmerProfileRepository.findById(id)
                .map(farmerProfilePersistenceMapper::toDomain);
    }

    @Override
    public Optional<FarmerProfile> findByUserId(UUID userId) {
        return jpaFarmerProfileRepository.findByUserId(userId)
                .map(farmerProfilePersistenceMapper::toDomain);
    }

    @Override
    public FarmerProfile save(FarmerProfile farmerProfile) {
        JpaFarmerProfileEntity entity = farmerProfilePersistenceMapper.toEntity(farmerProfile);
        JpaFarmerProfileEntity saved = jpaFarmerProfileRepository.save(entity);
        return farmerProfilePersistenceMapper.toDomain(saved);
    }
}
