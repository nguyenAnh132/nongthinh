package com.nongthinh.profile_service.infra.persistence.brandlifecyclelog;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.profile_service.application.port.out.repository.BrandLifecycleLogRepository;
import com.nongthinh.profile_service.domain.brandlifecyclelog.BrandLifecycleLog;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BrandLifecycleLogRepositoryImpl implements BrandLifecycleLogRepository {

    private final JpaBrandLifecycleLogRepository jpaBrandLifecycleLogRepository;
    private final BrandLifecycleLogPersistenceMapper brandLifecycleLogPersistenceMapper;

    @Override
    public BrandLifecycleLog save(BrandLifecycleLog brandLifecycleLog) {
        JpaBrandLifecycleLogEntity entity = brandLifecycleLogPersistenceMapper.toEntity(brandLifecycleLog);
        JpaBrandLifecycleLogEntity saved = jpaBrandLifecycleLogRepository.save(entity);
        return brandLifecycleLogPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<BrandLifecycleLog> findAllByBrandProfileIdOrderByCreatedAtDesc(UUID brandProfileId) {
        return jpaBrandLifecycleLogRepository.findAllByBrandProfileIdOrderByCreatedAtDesc(brandProfileId)
                .stream()
                .map(brandLifecycleLogPersistenceMapper::toDomain)
                .toList();
    }
}
