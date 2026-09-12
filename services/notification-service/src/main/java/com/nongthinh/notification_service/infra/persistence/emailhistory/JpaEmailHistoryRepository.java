package com.nongthinh.notification_service.infra.persistence.emailhistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaEmailHistoryRepository extends JpaRepository<JpaEmailHistoryEntity, Long> {

    List<JpaEmailHistoryEntity> findByUserId(UUID userId);

    List<JpaEmailHistoryEntity> findByTemplateId(Long templateId);

    List<JpaEmailHistoryEntity> findByPurposeId(Long purposeId);

}
