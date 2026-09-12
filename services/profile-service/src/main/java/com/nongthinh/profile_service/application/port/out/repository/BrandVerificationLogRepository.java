package com.nongthinh.profile_service.application.port.out.repository;

import java.util.List;
import java.util.UUID;
import com.nongthinh.profile_service.domain.brandverificationlog.BrandVerificationLog;

public interface BrandVerificationLogRepository {

    BrandVerificationLog save(BrandVerificationLog brandVerificationLog);

    List<BrandVerificationLog> findAllByBrandProfileIdOrderByVerifiedAtDesc(UUID brandProfileId);
}
