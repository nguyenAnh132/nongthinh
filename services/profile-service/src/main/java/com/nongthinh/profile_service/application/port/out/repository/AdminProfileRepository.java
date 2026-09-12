package com.nongthinh.profile_service.application.port.out.repository;

import java.util.Optional;
import java.util.UUID;
import com.nongthinh.profile_service.domain.adminprofile.AdminProfile;

public interface AdminProfileRepository {

    boolean existsByUserId(UUID userId);

    Optional<AdminProfile> findById(UUID id);

    Optional<AdminProfile> findByUserId(UUID userId);

    AdminProfile save(AdminProfile adminProfile);
}
