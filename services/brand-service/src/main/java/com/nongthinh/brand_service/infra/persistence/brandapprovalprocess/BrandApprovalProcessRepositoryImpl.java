package com.nongthinh.brand_service.infra.persistence.brandapprovalprocess;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.nongthinh.brand_service.application.port.out.repository.BrandApprovalProcessRepository;
import com.nongthinh.brand_service.domain.brandapprovalprocess.BrandApprovalProcess;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BrandApprovalProcessRepositoryImpl implements BrandApprovalProcessRepository {

    private final JpaBrandApprovalProcessRepository jpaBrandApprovalProcessRepository;
    private final BrandApprovalProcessPersistenceMapper brandApprovalProcessPersistenceMapper;

    @Override
    public boolean existsByBrandProfileId(UUID brandProfileId) {
        return jpaBrandApprovalProcessRepository.existsByBrandProfileId(brandProfileId);
    }

    @Override
    public Optional<BrandApprovalProcess> findByBrandProfileId(UUID brandProfileId) {
        return jpaBrandApprovalProcessRepository.findByBrandProfileId(brandProfileId)
                .map(brandApprovalProcessPersistenceMapper::toDomain);
    }

    @Override
    public BrandApprovalProcess save(BrandApprovalProcess brandApprovalProcess) {
        JpaBrandApprovalProcessEntity entity = brandApprovalProcessPersistenceMapper.toEntity(brandApprovalProcess);
        JpaBrandApprovalProcessEntity saved = jpaBrandApprovalProcessRepository.save(entity);
        return brandApprovalProcessPersistenceMapper.toDomain(saved);
    }
}
