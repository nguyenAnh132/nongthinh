package com.nongthinh.profile_service.infra.persistence.brandlifecyclelog;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaBrandLifecycleLogRepository extends JpaRepository<JpaBrandLifecycleLogEntity, UUID> {

    List<JpaBrandLifecycleLogEntity> findAllByBrandProfileIdOrderByCreatedAtDesc(UUID brandProfileId);
}
