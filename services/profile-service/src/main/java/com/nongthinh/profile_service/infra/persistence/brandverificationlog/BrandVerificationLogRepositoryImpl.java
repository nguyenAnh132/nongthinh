package com.nongthinh.profile_service.infra.persistence.brandverificationlog;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.profile_service.application.port.out.repository.BrandVerificationLogRepository;
import com.nongthinh.profile_service.domain.brandverificationlog.BrandVerificationLog;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BrandVerificationLogRepositoryImpl implements BrandVerificationLogRepository {

    private final JpaBrandVerificationLogRepository jpaBrandVerificationLogRepository;
    private final BrandVerificationLogPersistenceMapper brandVerificationLogPersistenceMapper;

    @Override
    public BrandVerificationLog save(BrandVerificationLog brandVerificationLog) {
        JpaBrandVerificationLogEntity entity = brandVerificationLogPersistenceMapper.toEntity(brandVerificationLog);
        JpaBrandVerificationLogEntity saved = jpaBrandVerificationLogRepository.save(entity);
        return brandVerificationLogPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<BrandVerificationLog> findAllByBrandProfileIdOrderByVerifiedAtDesc(UUID brandProfileId) {
        return jpaBrandVerificationLogRepository.findAllByBrandProfileIdOrderByVerifiedAtDesc(brandProfileId)
                .stream()
                .map(brandVerificationLogPersistenceMapper::toDomain)
                .toList();
    }
}
