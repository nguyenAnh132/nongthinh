package com.nongthinh.profile_service.infra.persistence.farmerprofile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaFarmerProfileRepository extends JpaRepository<JpaFarmerProfileEntity, UUID> {

    boolean existsByUserId(UUID userId);

    Optional<JpaFarmerProfileEntity> findByUserId(UUID userId);
}
