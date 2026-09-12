package com.nongthinh.brand_service.application.port.out.repository;

import java.util.Optional;
import java.util.UUID;

import com.nongthinh.brand_service.domain.brandapprovalprocess.BrandApprovalProcess;

public interface BrandApprovalProcessRepository {

    boolean existsByBrandProfileId(UUID brandProfileId);

    Optional<BrandApprovalProcess> findByBrandProfileId(UUID brandProfileId);

    BrandApprovalProcess save(BrandApprovalProcess brandApprovalProcess);
}
