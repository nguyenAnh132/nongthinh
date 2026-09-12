package com.nongthinh.profile_service.application.port.out.repository;

import java.util.Optional;
import java.util.UUID;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;

public interface FarmerProfileRepository {

    boolean existsByUserId(UUID userId);

    Optional<FarmerProfile> findById(UUID id);

    Optional<FarmerProfile> findByUserId(UUID userId);

    FarmerProfile save(FarmerProfile farmerProfile);
}
