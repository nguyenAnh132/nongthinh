package com.nongthinh.profile_service.infra.persistence.adminprofile;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaAdminProfileRepository extends JpaRepository<JpaAdminProfileEntity, UUID> {

    boolean existsByUserId(UUID userId);

    Optional<JpaAdminProfileEntity> findByUserId(UUID userId);
}
