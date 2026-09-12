package com.nongthinh.profile_service.application.port.out.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;

public interface BrandProfileRepository {

    boolean existsByUserId(UUID userId);

    Optional<BrandProfile> findById(UUID id);

    Optional<BrandProfile> findByUserId(UUID userId);

    List<BrandProfile> findAllByStatusIn(Collection<String> statuses);

    BrandProfile save(BrandProfile brandProfile);
}
