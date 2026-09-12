package com.nongthinh.profile_service.infra.persistence.adminprofile;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.profile_service.application.port.out.repository.AdminProfileRepository;
import com.nongthinh.profile_service.domain.adminprofile.AdminProfile;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminProfileRepositoryImpl implements AdminProfileRepository {

    private final JpaAdminProfileRepository jpaAdminProfileRepository;
    private final AdminProfilePersistenceMapper adminProfilePersistenceMapper;

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaAdminProfileRepository.existsByUserId(userId);
    }

    @Override
    public Optional<AdminProfile> findById(UUID id) {
        return jpaAdminProfileRepository.findById(id)
                .map(adminProfilePersistenceMapper::toDomain);
    }

    @Override
    public Optional<AdminProfile> findByUserId(UUID userId) {
        return jpaAdminProfileRepository.findByUserId(userId)
                .map(adminProfilePersistenceMapper::toDomain);
    }

    @Override
    public AdminProfile save(AdminProfile adminProfile) {
        JpaAdminProfileEntity entity = adminProfilePersistenceMapper.toEntity(adminProfile);
        JpaAdminProfileEntity saved = jpaAdminProfileRepository.save(entity);
        return adminProfilePersistenceMapper.toDomain(saved);
    }
}
