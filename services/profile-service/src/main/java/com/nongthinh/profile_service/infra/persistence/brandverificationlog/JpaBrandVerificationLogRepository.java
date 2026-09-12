package com.nongthinh.profile_service.infra.persistence.brandverificationlog;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaBrandVerificationLogRepository extends JpaRepository<JpaBrandVerificationLogEntity, UUID> {

    List<JpaBrandVerificationLogEntity> findAllByBrandProfileIdOrderByVerifiedAtDesc(UUID brandProfileId);
}
