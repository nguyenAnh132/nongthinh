package com.nongthinh.brand_service.infra.persistence.brandapprovalprocess;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBrandApprovalProcessRepository extends JpaRepository<JpaBrandApprovalProcessEntity, UUID> {

    boolean existsByBrandProfileId(UUID brandProfileId);

    Optional<JpaBrandApprovalProcessEntity> findByBrandProfileId(UUID brandProfileId);
}
