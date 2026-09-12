package com.nongthinh.profile_service.application.port.out.repository;

import java.util.List;
import java.util.UUID;
import com.nongthinh.profile_service.domain.brandlifecyclelog.BrandLifecycleLog;

public interface BrandLifecycleLogRepository {

    BrandLifecycleLog save(BrandLifecycleLog brandLifecycleLog);

    List<BrandLifecycleLog> findAllByBrandProfileIdOrderByCreatedAtDesc(UUID brandProfileId);
}
