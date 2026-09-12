package com.nongthinh.notification_service.infra.persistence.emailtemplate;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaEmailTemplateRepository extends JpaRepository<JpaEmailTemplateEntity, Long> {

    List<JpaEmailTemplateEntity> findByPurposeIdOrderByCreatedAtDesc(Long purposeId);

    Optional<JpaEmailTemplateEntity> findByPurposeIdAndActiveTrue(Long purposeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update JpaEmailTemplateEntity t
            set t.active = false, t.updatedAt = :updatedAt
            where t.purposeId = :purposeId and t.active = true
            """)
    void deactivateAllActiveByPurposeId(@Param("purposeId") Long purposeId, @Param("updatedAt") java.time.Instant updatedAt);
}
